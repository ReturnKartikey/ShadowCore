package com.shadowcore.app.engine

import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow

/**
 * Core engine interface for managing virtual environment (VE) containers.
 *
 * Defines the contract for booting, stopping, and managing apps within
 * isolated Android containers. The implementation will eventually delegate
 * to NewBlackbox (BlackBoxCore) for actual virtualization.
 *
 * All operations are scoped to a specific container identified by [veId].
 */
interface ContainerEngine {

    /**
     * Boots a virtual environment container using the specified system image.
     *
     * @param veId Unique identifier for the virtual environment
     * @param imagePath Absolute path to the extracted system image directory
     * @return A [Flow] of [BootEvent]s representing the boot lifecycle
     */
    fun boot(veId: String, imagePath: String): Flow<BootEvent>

    /**
     * Gracefully stops a running virtual environment.
     *
     * @param veId Unique identifier of the container to stop
     * @throws IllegalStateException if the container is not currently running
     */
    suspend fun stop(veId: String)

    /**
     * Checks whether a virtual environment is currently running.
     *
     * @param veId Unique identifier of the container
     * @return `true` if the container is actively running
     */
    fun isRunning(veId: String): Boolean

    /**
     * Installs an APK into a running virtual environment.
     *
     * @param veId Target container identifier
     * @param apkPath Absolute path to the APK file on the host filesystem
     * @return [Result] containing the installed package name on success,
     *         or an exception describing the failure
     */
    suspend fun installApp(veId: String, apkPath: String): Result<String>

    /**
     * Uninstalls an app from a virtual environment.
     *
     * @param veId Target container identifier
     * @param packageName Package name of the app to remove
     * @return [Result] indicating success or failure
     */
    suspend fun uninstallApp(veId: String, packageName: String): Result<Unit>

    /**
     * Lists all apps installed within a virtual environment.
     *
     * @param veId Target container identifier
     * @return List of [InstalledApp] entries for all installed packages
     */
    suspend fun listInstalledApps(veId: String): List<InstalledApp>
}

/**
 * Represents an app installed inside a virtual environment container.
 *
 * @property packageName Fully qualified package name (e.g., "com.example.app")
 * @property appName User-visible application label
 * @property icon Application icon drawable, or `null` if unavailable
 * @property versionName Human-readable version string (e.g., "1.2.3")
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val versionName: String,
)

/**
 * Events emitted during the virtual environment boot sequence.
 *
 * Boot progresses through: [Initializing] → [LoadingFramework] →
 * [StartingServices] → [Ready]. An [Error] can occur at any stage.
 */
sealed class BootEvent {

    /** Container is initializing its internal state and preparing the sandbox. */
    data object Initializing : BootEvent()

    /** Loading the Android framework JARs and system libraries from the image. */
    data object LoadingFramework : BootEvent()

    /** Starting core system services (PackageManager, ActivityManager, etc.). */
    data object StartingServices : BootEvent()

    /** Container is fully booted and ready to accept app installations and launches. */
    data object Ready : BootEvent()

    /**
     * An error occurred during boot.
     *
     * @property message Human-readable error description
     * @property cause The underlying exception, if available
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
    ) : BootEvent()
}
