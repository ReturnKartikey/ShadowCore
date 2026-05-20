package com.shadowcore.app.ui.screens.vmdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.repository.VmRepository
import com.shadowcore.app.vm.VmEngineResult
import com.shadowcore.app.vm.VmManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VmDetailUiState(
    val profile: VmProfile? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
    val isDeleted: Boolean = false,
    val isStarting: Boolean = false,
)

@HiltViewModel
class VmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vmRepository: VmRepository,
    private val vmManager: VmManager,
) : ViewModel() {

    private val vmId: String = savedStateHandle["vmId"] ?: ""
    private val _uiState = MutableStateFlow(VmDetailUiState())
    val uiState: StateFlow<VmDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vmRepository.getVmProfileById(vmId).collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    fun startVm() = viewModelScope.launch {
        _uiState.update { it.copy(isStarting = true) }
        when (val r = vmManager.startVm(vmId)) {
            is VmEngineResult.Success -> {
                _uiState.update { it.copy(message = "✓ VM is now running", isStarting = false) }
            }
            is VmEngineResult.Error ->
                _uiState.update { it.copy(message = "✗ ${r.message}", isStarting = false) }
            is VmEngineResult.Unsupported ->
                _uiState.update { it.copy(message = "✗ ${r.message}", isStarting = false) }
            is VmEngineResult.InsufficientResources ->
                _uiState.update { it.copy(message = "✗ ${r.message}", isStarting = false) }
        }
    }

    fun stopVm() = viewModelScope.launch {
        when (val r = vmManager.stopVm(vmId)) {
            is VmEngineResult.Success ->
                _uiState.update { it.copy(message = "VM stopped") }
            else ->
                _uiState.update { it.copy(message = "Failed to stop VM") }
        }
    }

    fun pauseVm() = viewModelScope.launch {
        when (val r = vmManager.pauseVm(vmId)) {
            is VmEngineResult.Success ->
                _uiState.update { it.copy(message = "VM paused") }
            is VmEngineResult.Error ->
                _uiState.update { it.copy(message = r.message) }
            else -> {}
        }
    }

    fun repairVm() = viewModelScope.launch {
        vmManager.repairVm(vmId)
        _uiState.update { it.copy(message = "VM repaired successfully") }
    }

    fun deleteVm() = viewModelScope.launch {
        vmManager.stopVm(vmId) // Stop first if running
        vmRepository.deleteVmProfile(vmId)
        _uiState.update { it.copy(isDeleted = true) }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null) } }
}
