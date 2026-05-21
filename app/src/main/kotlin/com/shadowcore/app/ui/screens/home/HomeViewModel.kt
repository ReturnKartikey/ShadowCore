package com.shadowcore.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.domain.model.CapabilityTier
import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.usecase.GetVeProfilesUseCase
import com.shadowcore.app.engine.ContainerManager
import com.shadowcore.app.vm.VmCapabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val veProfiles: List<VeProfile> = emptyList(),
    val isLoading: Boolean = true,
    val capabilityTier: CapabilityTier = CapabilityTier.TIER_0_UNSUPPORTED,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVeProfiles: GetVeProfilesUseCase,
    private val containerManager: ContainerManager,
    private val capabilityChecker: VmCapabilityChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(capabilityTier = capabilityChecker.capabilityTier) }

        // Observe VE profiles
        viewModelScope.launch {
            getVeProfiles().collect { profiles ->
                _uiState.update {
                    it.copy(veProfiles = profiles, isLoading = false)
                }
            }
        }
    }

    fun startVe(veId: String) {
        viewModelScope.launch {
            try {
                containerManager.startContainer(veId)
                _uiState.update { it.copy(successMessage = "Virtual environment started") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to start") }
            }
        }
    }

    fun stopVe(veId: String) {
        viewModelScope.launch {
            try {
                containerManager.stopContainer(veId)
                _uiState.update { it.copy(successMessage = "Virtual environment stopped") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to stop") }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
}
