package com.shadowcore.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.data.preferences.AppPreferences
import com.shadowcore.app.vm.CapabilityReport
import com.shadowcore.app.vm.VmCapabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: Int = 0, // 0=Auto, 1=Light, 2=Dark
    val capabilityReport: CapabilityReport? = null,
    val appVersion: String = "1.0.0",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val capabilityChecker: VmCapabilityChecker,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(capabilityReport = capabilityChecker.capabilityReport)
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            appPreferences.setThemeMode(mode)
        }
    }
}
