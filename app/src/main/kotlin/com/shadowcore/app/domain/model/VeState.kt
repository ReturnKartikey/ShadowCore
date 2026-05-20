package com.shadowcore.app.domain.model

/**
 * Represents the current state of a virtual environment instance.
 * Used with MVI pattern — state transitions are strictly controlled.
 */
sealed class VeState(val displayName: String) {
    data object Stopped : VeState("Stopped")
    data object Starting : VeState("Starting…")
    data object Running : VeState("Running")
    data object Paused : VeState("Paused")
    data object Downloading : VeState("Downloading…")
    data class Error(val message: String) : VeState("Error")

    val isActive: Boolean
        get() = this is Running || this is Starting || this is Paused || this is Downloading

    val canStart: Boolean
        get() = this is Stopped || this is Error

    val canStop: Boolean
        get() = this is Running || this is Paused || this is Starting

    val canPause: Boolean
        get() = this is Running
}
