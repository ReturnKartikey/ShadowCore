package com.shadowcore.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.domain.model.CapabilityTier
import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.usecase.GetVmProfilesUseCase
import com.shadowcore.app.vm.VmCapabilityChecker
import com.shadowcore.app.vm.VmEngineResult
import com.shadowcore.app.vm.VmManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val vmProfiles: List<VmProfile> = emptyList(),
    val isLoading: Boolean = true,
    val capabilityTier: CapabilityTier = CapabilityTier.TIER_0_UNSUPPORTED,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVmProfiles: GetVmProfilesUseCase,
    private val vmManager: VmManager,
    private val capabilityChecker: VmCapabilityChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(capabilityTier = capabilityChecker.capabilityTier) }

        // Observe VM profiles
        viewModelScope.launch {
            getVmProfiles().collect { profiles ->
                _uiState.update {
                    it.copy(vmProfiles = profiles, isLoading = false)
                }
            }
        }

        // Crash recovery
        viewModelScope.launch {
            vmManager.recoverFromCrash()
        }
    }

    fun startVm(vmId: String) {
        viewModelScope.launch {
            val result = vmManager.startVm(vmId)
            when (result) {
                is VmEngineResult.Success ->
                    _uiState.update { it.copy(successMessage = "VM started — ${result.profile.name} is running") }
                is VmEngineResult.Error ->
                    _uiState.update { it.copy(errorMessage = result.message) }
                is VmEngineResult.Unsupported ->
                    _uiState.update { it.copy(errorMessage = result.message) }
                is VmEngineResult.InsufficientResources ->
                    _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun stopVm(vmId: String) {
        viewModelScope.launch {
            val result = vmManager.stopVm(vmId)
            if (result is VmEngineResult.Success) {
                _uiState.update { it.copy(successMessage = "VM stopped") }
            }
        }
    }

    fun deleteVm(vmId: String) {
        viewModelScope.launch {
            vmManager.stopVm(vmId)
            // Delete handled by detail screen
        }
    }

    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
    fun clearSuccess() { _uiState.update { it.copy(successMessage = null) } }
}
