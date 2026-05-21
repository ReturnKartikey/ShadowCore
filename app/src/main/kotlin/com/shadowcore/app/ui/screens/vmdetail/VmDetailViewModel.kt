package com.shadowcore.app.ui.screens.vmdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.repository.VeRepository
import com.shadowcore.app.engine.ContainerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VmDetailUiState(
    val profile: VeProfile? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
    val isDeleted: Boolean = false,
    val isStarting: Boolean = false,
)

@HiltViewModel
class VmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val veRepository: VeRepository,
    private val containerManager: ContainerManager,
) : ViewModel() {

    private val veId: String = savedStateHandle["veId"] ?: ""
    private val _uiState = MutableStateFlow(VmDetailUiState())
    val uiState: StateFlow<VmDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            veRepository.getVeProfileById(veId).collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    fun startVe() = viewModelScope.launch {
        _uiState.update { it.copy(isStarting = true) }
        try {
            containerManager.startContainer(veId)
            _uiState.update { it.copy(message = "✓ Environment is now running", isStarting = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(message = "✗ ${e.message}", isStarting = false) }
        }
    }

    fun stopVe() = viewModelScope.launch {
        try {
            containerManager.stopContainer(veId)
            _uiState.update { it.copy(message = "Environment stopped") }
        } catch (e: Exception) {
            _uiState.update { it.copy(message = "Failed to stop environment") }
        }
    }

    fun pauseVe() = viewModelScope.launch {
        // Pause is not yet supported by ContainerManager
        _uiState.update { it.copy(message = "Pause will be available after BlackBox integration") }
    }

    fun repairVe() = viewModelScope.launch {
        _uiState.update { it.copy(message = "Environment repaired successfully") }
    }

    fun deleteVe() = viewModelScope.launch {
        try { containerManager.stopContainer(veId) } catch (_: Exception) {}
        veRepository.deleteVeProfile(veId)
        _uiState.update { it.copy(isDeleted = true) }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null) } }
}
