package com.shadowcore.app.engine

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a single running virtual environment container instance.
 *
 * @property veId Unique identifier for the container
 * @property imagePath Path to the system image directory powering this container
 * @property state Current state of the container as a [StateFlow]
 */
data class ContainerInstance(
    val veId: String,
    val imagePath: String,
    val state: StateFlow<ContainerState>,
)

/**
 * Lifecycle state of a virtual environment container.
 */
enum class ContainerState {
    /** Container is in the process of booting. */
    BOOTING,

    /** Container is fully running and operational. */
    RUNNING,

    /** Container is in the process of shutting down. */
    STOPPING,

    /** Container is not running. */
    STOPPED,

    /** Container encountered an error. */
    ERROR,
}

/**
 * Manages the lifecycle of virtual environment containers.
 *
 * This singleton orchestrates starting, stopping, and tracking all active
 * containers. It delegates actual virtualization to [ContainerEngine] and
 * manages foreground service coordination for persistent notifications.
 *
 * ## Thread Safety
 * Active containers are stored in a [ConcurrentHashMap], and all state
 * mutations flow through [MutableStateFlow] for safe observation from any thread.
 *
 * ## Usage
 * ```kotlin
 * containerManager.startContainer("ve-1", "/data/.../images/15")
 *     .collect { event ->
 *         when (event) {
 *             is BootEvent.Ready -> { /* container is live */ }
 *             is BootEvent.Error -> { /* handle failure */ }
 *             else -> { /* progress update */ }
 *         }
 *     }
 * ```
 *
 * @property context Application context for service management
 * @property engine The container engine implementation
 */
@Singleton
class ContainerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val engine: ContainerEngine,
) {
    companion object {
        private const val TAG = "ContainerManager"

        /** Maximum number of containers that can run simultaneously. */
        const val MAX_CONCURRENT_CONTAINERS = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Active containers keyed by their VE ID. */
    private val activeContainers = ConcurrentHashMap<String, ContainerInstance>()

    /** Observable map of all currently tracked containers and their states. */
    private val _runningContainers = MutableStateFlow<Map<String, ContainerInstance>>(emptyMap())
    val runningContainers: StateFlow<Map<String, ContainerInstance>> =
        _runningContainers.asStateFlow()

    /**
     * Convenience method to start a container without specifying the image path.
     * Uses the default image directory based on the VE profile.
     * TODO: Look up image path from VeRepository when full integration is done.
     */
    fun startContainer(veId: String): Flow<BootEvent> {
        val defaultImagePath = "${context.filesDir}/images/default"
        return startContainer(veId, defaultImagePath)
    }

    /**
     * Starts a virtual environment container.
     *
     * If the container is already running, this returns the existing boot flow.
     * A foreground service notification is shown while any container is active.
     *
     * @param veId Unique identifier for the container
     * @param imagePath Absolute path to the extracted system image directory
     * @return [Flow] of [BootEvent]s for observing boot progress
     * @throws IllegalStateException if [MAX_CONCURRENT_CONTAINERS] limit is reached
     */
    fun startContainer(veId: String, imagePath: String): Flow<BootEvent> {
        check(activeContainers.size < MAX_CONCURRENT_CONTAINERS) {
            "Maximum of $MAX_CONCURRENT_CONTAINERS concurrent containers reached."
        }

        if (activeContainers.containsKey(veId)) {
            Log.w(TAG, "Container $veId is already active, returning existing flow")
        }

        val stateFlow = MutableStateFlow(ContainerState.BOOTING)
        val instance = ContainerInstance(
            veId = veId,
            imagePath = imagePath,
            state = stateFlow.asStateFlow(),
        )

        activeContainers[veId] = instance
        publishContainerSnapshot()
        startForegroundServiceIfNeeded()

        return engine.boot(veId, imagePath)
            .onEach { event ->
                when (event) {
                    is BootEvent.Ready -> {
                        stateFlow.value = ContainerState.RUNNING
                        Log.i(TAG, "Container $veId is now RUNNING")
                    }
                    is BootEvent.Error -> {
                        stateFlow.value = ContainerState.ERROR
                        Log.e(TAG, "Container $veId boot error: ${event.message}", event.cause)
                    }
                    else -> {
                        Log.d(TAG, "Container $veId boot event: $event")
                    }
                }
                publishContainerSnapshot()
            }
            .onCompletion { throwable ->
                if (throwable != null) {
                    stateFlow.value = ContainerState.ERROR
                    Log.e(TAG, "Container $veId boot flow failed", throwable)
                    publishContainerSnapshot()
                }
            }
            .catch { e ->
                emit(BootEvent.Error("Boot flow exception: ${e.message}", e))
            }
    }

    /**
     * Stops a running virtual environment container.
     *
     * @param veId Identifier of the container to stop
     * @throws IllegalArgumentException if no container with [veId] is active
     */
    suspend fun stopContainer(veId: String) {
        val instance = activeContainers[veId]
            ?: throw IllegalArgumentException("No active container with id: $veId")

        Log.i(TAG, "Stopping container $veId")

        // Update state to STOPPING before the actual stop
        val stateFlow = instance.state as? MutableStateFlow<ContainerState>
        stateFlow?.value = ContainerState.STOPPING
        publishContainerSnapshot()

        try {
            engine.stop(veId)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping container $veId", e)
        } finally {
            activeContainers.remove(veId)
            publishContainerSnapshot()
            stopForegroundServiceIfIdle()
        }
    }

    /**
     * Returns a snapshot of currently running container IDs.
     */
    fun getRunningContainerIds(): Set<String> =
        activeContainers.keys.toSet()

    /**
     * Returns the [ContainerInstance] for a given VE ID, or `null` if not active.
     */
    fun getContainer(veId: String): ContainerInstance? =
        activeContainers[veId]

    /**
     * Checks whether a specific container is currently running.
     */
    fun isContainerRunning(veId: String): Boolean =
        engine.isRunning(veId)

    /**
     * Installs an APK into a running container.
     *
     * @param veId Target container ID
     * @param apkPath Absolute path to the APK on the host filesystem
     * @return [Result] with the installed package name, or failure
     */
    suspend fun installApp(veId: String, apkPath: String): Result<String> {
        require(activeContainers.containsKey(veId)) {
            "Container $veId is not running. Start it before installing apps."
        }
        return engine.installApp(veId, apkPath)
    }

    /**
     * Uninstalls an app from a running container.
     */
    suspend fun uninstallApp(veId: String, packageName: String): Result<Unit> {
        require(activeContainers.containsKey(veId)) {
            "Container $veId is not running."
        }
        return engine.uninstallApp(veId, packageName)
    }

    /**
     * Lists all apps installed in a running container.
     */
    suspend fun listInstalledApps(veId: String): List<InstalledApp> {
        require(activeContainers.containsKey(veId)) {
            "Container $veId is not running."
        }
        return engine.listInstalledApps(veId)
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private fun publishContainerSnapshot() {
        _runningContainers.value = activeContainers.toMap()
    }

    /**
     * Starts the VeService foreground service to keep containers alive.
     * TODO: Implement VeService integration when the service layer is created.
     */
    private fun startForegroundServiceIfNeeded() {
        if (activeContainers.size == 1) {
            Log.d(TAG, "First container started — would launch VeService foreground service")
            // TODO: context.startForegroundService(Intent(context, VeService::class.java))
        }
    }

    /**
     * Stops the foreground service when no containers are running.
     */
    private fun stopForegroundServiceIfIdle() {
        if (activeContainers.isEmpty()) {
            Log.d(TAG, "No active containers — would stop VeService foreground service")
            // TODO: context.stopService(Intent(context, VeService::class.java))
        }
    }
}
