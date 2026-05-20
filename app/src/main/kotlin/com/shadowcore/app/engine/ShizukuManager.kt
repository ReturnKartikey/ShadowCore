package com.shadowcore.app.engine

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the Shizuku activation lifecycle for obtaining ADB-level privileges.
 *
 * Shizuku allows apps to execute privileged operations via Wireless Debugging
 * without requiring root access. This manager handles:
 * - Detecting whether Shizuku is installed and running
 * - Requesting runtime permission from the Shizuku service
 * - Checking Wireless Debugging availability
 * - Tracking the full activation state machine via [ActivationState]
 *
 * ## Reflection-Based Design
 * Since the Shizuku SDK may not be present at compile time, all API calls are
 * performed via reflection using [Class.forName("rikka.shizuku.Shizuku")].
 * This ensures the app compiles and runs gracefully without the SDK, falling
 * back to [ActivationState.ShizukuNotInstalled] when Shizuku classes are absent.
 *
 * ## Shizuku APIs Used (via reflection)
 * - `Shizuku.pingBinder()` → check if the service is running
 * - `Shizuku.checkSelfPermission()` → check if permission is granted
 * - `Shizuku.requestPermission(int)` → request permission from the user
 * - `Shizuku.addBinderReceivedListener(Runnable)` → observe binder availability
 * - `Shizuku.addRequestPermissionResultListener(OnRequestPermissionResultListener)` → permission callbacks
 *
 * @property context Application context for package manager and settings access
 */
@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "ShizukuManager"
        private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        private const val SHIZUKU_CLASS = "rikka.shizuku.Shizuku"
        private const val REQUEST_CODE = 42_000

        /**
         * Permission constant matching `ShizukuProvider.PERMISSION`.
         * This is the Android permission declared by Shizuku for runtime access.
         */
        private const val SHIZUKU_PERMISSION = "moe.shizuku.manager.permission.API_V23"
    }

    /** Whether the Shizuku binder is reachable (service is running). */
    private val _isShizukuAvailable = MutableStateFlow(false)
    val isShizukuAvailable: StateFlow<Boolean> = _isShizukuAvailable.asStateFlow()

    /** Whether the Shizuku permission has been granted to this app. */
    private val _isShizukuGranted = MutableStateFlow(false)
    val isShizukuGranted: StateFlow<Boolean> = _isShizukuGranted.asStateFlow()

    /** The current state of the activation flow. */
    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.NotStarted)
    val activationState: StateFlow<ActivationState> = _activationState.asStateFlow()

    /** Cached reference to the Shizuku class, or `null` if not available. */
    private var shizukuClass: Class<*>? = null

    init {
        // Attempt to load Shizuku class at construction time
        shizukuClass = try {
            Class.forName(SHIZUKU_CLASS)
        } catch (_: ClassNotFoundException) {
            Log.d(TAG, "Shizuku SDK not found on classpath")
            null
        }
    }

    /**
     * Performs a full check of Shizuku status and updates all state flows.
     *
     * Call this from your activation screen's `onResume` or when the user
     * taps a "Check Status" button.
     *
     * State transitions:
     * ```
     * NotStarted → CheckingShizuku → (one of)
     *   ├─ ShizukuNotInstalled  (if app not installed)
     *   ├─ WirelessDebuggingDisabled (if wireless debugging off)
     *   ├─ ShizukuNotRunning    (if binder not reachable)
     *   ├─ Activated            (if permission already granted)
     *   └─ RequestingPermission (if binder up but no permission)
     * ```
     */
    fun checkShizukuStatus() {
        _activationState.value = ActivationState.CheckingShizuku
        Log.d(TAG, "Checking Shizuku status...")

        // Step 1: Is Shizuku app installed?
        if (!isShizukuAppInstalled()) {
            _isShizukuAvailable.value = false
            _isShizukuGranted.value = false
            _activationState.value = ActivationState.ShizukuNotInstalled
            Log.i(TAG, "Shizuku app is not installed")
            return
        }

        // Step 2: Is Wireless Debugging enabled?
        if (!isWirelessDebuggingEnabled(context)) {
            _activationState.value = ActivationState.WirelessDebuggingDisabled
            Log.i(TAG, "Wireless Debugging is not enabled")
            return
        }

        // Step 3: Is the Shizuku binder alive?
        val binderAlive = pingBinder()
        _isShizukuAvailable.value = binderAlive

        if (!binderAlive) {
            _isShizukuGranted.value = false
            _activationState.value = ActivationState.ShizukuNotRunning
            Log.i(TAG, "Shizuku binder is not reachable")
            return
        }

        // Step 4: Do we have permission?
        val hasPermission = checkPermission()
        _isShizukuGranted.value = hasPermission

        if (hasPermission) {
            _activationState.value = ActivationState.Activated
            Log.i(TAG, "Shizuku is active and permission granted")
        } else {
            _activationState.value = ActivationState.RequestingPermission
            Log.i(TAG, "Shizuku is running but permission not yet granted")
        }
    }

    /**
     * Requests Shizuku permission from the user.
     *
     * This triggers Shizuku's permission dialog. The result is delivered
     * asynchronously — call [checkShizukuStatus] after the user responds,
     * or register a Shizuku `OnRequestPermissionResultListener`.
     *
     * Does nothing if the Shizuku SDK is not available.
     */
    fun requestPermission() {
        _activationState.value = ActivationState.RequestingPermission
        Log.d(TAG, "Requesting Shizuku permission...")

        try {
            val clazz = shizukuClass ?: run {
                _activationState.value = ActivationState.ShizukuNotInstalled
                return
            }
            val method = clazz.getDeclaredMethod("requestPermission", Int::class.java)
            method.invoke(null, REQUEST_CODE)
            Log.i(TAG, "Permission request sent with code $REQUEST_CODE")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
            _activationState.value = ActivationState.Error(
                "Failed to request permission: ${e.message}"
            )
        }
    }

    /**
     * Handles the result of a Shizuku permission request.
     *
     * Call this from your Activity's `onRequestPermissionsResult` or from
     * a registered Shizuku `OnRequestPermissionResultListener`.
     *
     * @param requestCode The request code from the callback
     * @param grantResult The grant result (e.g., [PackageManager.PERMISSION_GRANTED])
     */
    fun onPermissionResult(requestCode: Int, grantResult: Int) {
        if (requestCode != REQUEST_CODE) return

        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        _isShizukuGranted.value = granted

        _activationState.value = if (granted) {
            Log.i(TAG, "Shizuku permission granted")
            ActivationState.Activated
        } else {
            Log.w(TAG, "Shizuku permission denied")
            ActivationState.PermissionDenied
        }
    }

    /**
     * Checks whether Wireless Debugging (ADB over WiFi) is enabled.
     *
     * This reads the `adb_wifi_enabled` system setting introduced in Android 11.
     * On Android 10 and below, this always returns `true` since Wireless Debugging
     * was not a prerequisite in those versions.
     *
     * @param context Context for accessing [Settings.Global]
     * @return `true` if Wireless Debugging is enabled or not applicable
     */
    fun isWirelessDebuggingEnabled(context: Context): Boolean {
        return try {
            // "adb_wifi_enabled" is the setting for Wireless Debugging (API 30+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Settings.Global.getInt(
                    context.contentResolver,
                    "adb_wifi_enabled",
                    0
                ) == 1
            } else {
                // Pre-Android 11: wireless debugging wasn't a separate toggle
                true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read Wireless Debugging setting", e)
            false
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────

    /**
     * Checks if the Shizuku Manager app is installed on the device.
     */
    private fun isShizukuAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Calls `Shizuku.pingBinder()` via reflection to check if the service is alive.
     */
    private fun pingBinder(): Boolean {
        return try {
            val clazz = shizukuClass ?: return false
            val method = clazz.getDeclaredMethod("pingBinder")
            method.invoke(null) as? Boolean ?: false
        } catch (e: Exception) {
            Log.w(TAG, "pingBinder() failed", e)
            false
        }
    }

    /**
     * Calls `Shizuku.checkSelfPermission()` via reflection to verify permission state.
     * Returns `true` if the result equals [PackageManager.PERMISSION_GRANTED].
     */
    private fun checkPermission(): Boolean {
        return try {
            val clazz = shizukuClass ?: return false
            val method = clazz.getDeclaredMethod("checkSelfPermission")
            val result = method.invoke(null) as? Int ?: return false
            result == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.w(TAG, "checkSelfPermission() failed", e)
            false
        }
    }
}
