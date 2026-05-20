package com.shadowcore.app.domain.model

/**
 * Represents the current state of a virtual machine instance.
 * Used with MVI pattern — state transitions are strictly controlled.
 */
sealed class VmState(val displayName: String) {
    data object Stopped : VmState("Stopped")
    data object Starting : VmState("Starting…")
    data object Running : VmState("Running")
    data object Paused : VmState("Paused")
    data object Downloading : VmState("Downloading…")
    data class Error(val message: String) : VmState("Error")

    val isActive: Boolean
        get() = this is Running || this is Starting || this is Paused || this is Downloading

    val canStart: Boolean
        get() = this is Stopped || this is Error

    val canStop: Boolean
        get() = this is Running || this is Paused || this is Starting

    val canPause: Boolean
        get() = this is Running
}
