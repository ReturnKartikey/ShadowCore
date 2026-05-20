package com.shadowcore.app.ui.screens.activation

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.engine.ActivationState
import com.shadowcore.app.engine.ShizukuManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivationUiState(
    val devOptionsEnabled: Boolean = false,
    val wirelessDebuggingEnabled: Boolean = false,
    val shizukuInstalled: Boolean = false,
    val shizukuRunning: Boolean = false,
    val activationState: ActivationState = ActivationState.NotStarted,
)

@HiltViewModel
class ActivationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shizukuManager: ShizukuManager,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivationUiState())
    val state: StateFlow<ActivationUiState> = _state.asStateFlow()

    init {
        // Observe Shizuku state
        viewModelScope.launch {
            shizukuManager.activationState.collect { activation ->
                _state.update { it.copy(activationState = activation) }
            }
        }

        // Run initial checks
        checkDeveloperOptions()
    }

    fun checkDeveloperOptions() {
        val enabled = try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (_: Exception) {
            false
        }
        _state.update { it.copy(devOptionsEnabled = enabled) }

        if (enabled) {
            checkWirelessDebugging()
        }
    }

    fun checkWirelessDebugging() {
        val enabled = shizukuManager.isWirelessDebuggingEnabled(context)
        _state.update { it.copy(wirelessDebuggingEnabled = enabled) }

        if (enabled) {
            checkShizukuInstalled()
        }
    }

    fun checkShizukuInstalled() {
        val installed = try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            false
        }
        _state.update { it.copy(shizukuInstalled = installed) }

        if (installed) {
            checkShizukuRunning()
        }
    }

    fun checkShizukuRunning() {
        shizukuManager.checkShizukuStatus()
        val running = shizukuManager.isShizukuAvailable.value
        _state.update { it.copy(shizukuRunning = running) }
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }
}
