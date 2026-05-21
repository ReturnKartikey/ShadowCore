package com.shadowcore.app.engine

import android.graphics.drawable.Drawable
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of [ContainerEngine] for development.
 *
 * Simulates the boot/stop lifecycle with delays and log output.
 * Will be replaced by the real BlackBox-based engine once integrated.
 */
@Singleton
class StubContainerEngine @Inject constructor() : ContainerEngine {

    companion object {
        private const val TAG = "StubContainerEngine"
    }

    private val runningContainers = mutableSetOf<String>()

    override fun boot(veId: String, imagePath: String): Flow<BootEvent> = flow {
        Log.i(TAG, "Booting container $veId from image: $imagePath")

        emit(BootEvent.Initializing)
        delay(500)

        emit(BootEvent.LoadingFramework)
        delay(800)

        emit(BootEvent.StartingServices)
        delay(600)

        runningContainers.add(veId)
        emit(BootEvent.Ready)
        Log.i(TAG, "Container $veId boot complete")
    }

    override suspend fun stop(veId: String) {
        Log.i(TAG, "Stopping container $veId")
        runningContainers.remove(veId)
    }

    override fun isRunning(veId: String): Boolean =
        veId in runningContainers

    override suspend fun installApp(veId: String, apkPath: String): Result<String> {
        Log.i(TAG, "Stub: installApp $apkPath into $veId")
        return Result.success("com.stub.installed")
    }

    override suspend fun uninstallApp(veId: String, packageName: String): Result<Unit> {
        Log.i(TAG, "Stub: uninstallApp $packageName from $veId")
        return Result.success(Unit)
    }

    override suspend fun listInstalledApps(veId: String): List<InstalledApp> {
        return listOf(
            InstalledApp("com.android.settings", "Settings", null, "1.0"),
            InstalledApp("com.android.calculator2", "Calculator", null, "1.0"),
            InstalledApp("com.android.contacts", "Contacts", null, "1.0"),
        )
    }
}
