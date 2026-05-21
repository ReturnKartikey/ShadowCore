package com.shadowcore.app.ui.screens.createvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.domain.model.*
import com.shadowcore.app.domain.usecase.CreateVeUseCase
import com.shadowcore.app.vm.VmCapabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateVmUiState(
    val name: String = "",
    val selectedVersion: AndroidVersion = AndroidVersion.R_11,
    val selectedCategory: VeCategory = VeCategory.PERSONAL,
    val allocatedRamMb: Int = 2048,
    val allocatedStorageMb: Int = 8192,
    val isNetworkIsolated: Boolean = false,
    val hasRootAccess: Boolean = false,
    val capabilityTier: CapabilityTier = CapabilityTier.TIER_1_CONTAINER,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val isCreated: Boolean = false,
)

@HiltViewModel
class CreateVmViewModel @Inject constructor(
    private val createVeUseCase: CreateVeUseCase,
    private val capabilityChecker: VmCapabilityChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVmUiState())
    val uiState: StateFlow<CreateVmUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(capabilityTier = capabilityChecker.capabilityTier) }
    }

    fun updateName(name: String) { _uiState.update { it.copy(name = name) } }
    fun selectVersion(v: AndroidVersion) { _uiState.update { it.copy(selectedVersion = v) } }
    fun selectCategory(c: VeCategory) { _uiState.update { it.copy(selectedCategory = c) } }
    fun updateRam(mb: Int) { _uiState.update { it.copy(allocatedRamMb = mb) } }
    fun updateStorage(mb: Int) { _uiState.update { it.copy(allocatedStorageMb = mb) } }
    fun toggleNetworkIsolation() { _uiState.update { it.copy(isNetworkIsolated = !it.isNetworkIsolated) } }
    fun toggleRootAccess() { _uiState.update { it.copy(hasRootAccess = !it.hasRootAccess) } }

    fun createVe() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an environment name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }

            val profile = VeProfile(
                id = UUID.randomUUID().toString(),
                name = state.name,
                androidVersion = state.selectedVersion,
                category = state.selectedCategory,
                allocatedRamMb = state.allocatedRamMb,
                allocatedStorageMb = state.allocatedStorageMb,
                isNetworkIsolated = state.isNetworkIsolated,
                hasRootAccess = state.hasRootAccess,
                executionTier = state.capabilityTier,
            )

            when (val result = createVeUseCase(profile)) {
                is CreateVeUseCase.Result.Success ->
                    _uiState.update { it.copy(isCreating = false, isCreated = true) }
                is CreateVeUseCase.Result.Error ->
                    _uiState.update { it.copy(isCreating = false, errorMessage = result.message) }
            }
        }
    }
}
