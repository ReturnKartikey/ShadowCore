package com.shadowcore.app.engine

/**
 * Represents the current state of the Shizuku activation flow.
 *
 * The activation process follows this sequence:
 * 1. [NotStarted] → Initial state before any checks
 * 2. [CheckingShizuku] → Verifying Shizuku availability and status
 * 3. One of:
 *    - [ShizukuNotInstalled] → Shizuku app is not present on device
 *    - [WirelessDebuggingDisabled] → Wireless debugging is not enabled in developer settings
 *    - [ShizukuNotRunning] → Shizuku is installed but service is not active
 *    - [RequestingPermission] → Awaiting user grant of Shizuku permission
 * 4. Terminal states:
 *    - [Activated] → Full ADB-level privileges acquired
 *    - [PermissionDenied] → User denied Shizuku permission
 *    - [Error] → An unexpected error occurred
 */
sealed class ActivationState {

    /** Initial state — no activation checks have been performed yet. */
    data object NotStarted : ActivationState()

    /** Actively checking Shizuku installation and binder status. */
    data object CheckingShizuku : ActivationState()

    /** Shizuku app is not installed on the device. */
    data object ShizukuNotInstalled : ActivationState()

    /** Wireless Debugging is disabled in Developer Options. */
    data object WirelessDebuggingDisabled : ActivationState()

    /** Shizuku is installed but the service is not running. */
    data object ShizukuNotRunning : ActivationState()

    /** Permission request has been sent to the user. */
    data object RequestingPermission : ActivationState()

    /** User denied the Shizuku permission request. */
    data object PermissionDenied : ActivationState()

    /** Shizuku is running and permission is granted — full activation achieved. */
    data object Activated : ActivationState()

    /** An unexpected error occurred during activation. */
    data class Error(val message: String) : ActivationState()
}
