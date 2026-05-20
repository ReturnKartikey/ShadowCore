package com.shadowcore.app.ui.screens.container

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.engine.ContainerManager
import com.shadowcore.app.engine.ImageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContainerUiState(
    val veId: String = "",
    val androidVersion: String = "Android 15",
    val isBooting: Boolean = false,
    val isRunning: Boolean = false,
    val bootPhase: String = "",
    val bootProgress: Float = 0f,
    val installedApps: List<VirtualApp> = emptyList(),
    val showInstallDialog: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ContainerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val containerManager: ContainerManager,
    private val imageManager: ImageManager,
) : ViewModel() {

    private val veId: String = savedStateHandle["veId"] ?: ""

    private val _state = MutableStateFlow(ContainerUiState(veId = veId))
    val state: StateFlow<ContainerUiState> = _state.asStateFlow()

    init {
        // Auto-start boot sequence
        viewModelScope.launch {
            delay(500) // Brief delay for screen transition
            startContainer()
        }
    }

    fun startContainer() {
        if (_state.value.isBooting || _state.value.isRunning) return

        viewModelScope.launch {
            _state.update { it.copy(isBooting = true, bootProgress = 0f) }

            // Simulate boot phases (will be replaced by real BlackBox boot)
            val phases = listOf(
                "Checking system image..." to 0.1f,
                "Loading framework JARs..." to 0.2f,
                "Initializing container engine..." to 0.35f,
                "Starting Zygote process..." to 0.5f,
                "Launching SystemServer..." to 0.65f,
                "Starting package manager..." to 0.75f,
                "Loading system apps..." to 0.85f,
                "Starting launcher..." to 0.95f,
                "Virtual Environment ready" to 1.0f,
            )

            for ((phase, progress) in phases) {
                _state.update { it.copy(bootPhase = phase, bootProgress = progress) }
                delay((300L..800L).random())
            }

            delay(400)
            _state.update {
                it.copy(
                    isBooting = false,
                    isRunning = true,
                    bootPhase = "",
                    installedApps = getDefaultApps(),
                )
            }
        }
    }

    fun stopContainer() {
        viewModelScope.launch {
            _state.update { it.copy(isRunning = false, installedApps = emptyList()) }
        }
    }

    fun launchApp(packageName: String) {
        // TODO: Launch app inside container via BlackBox engine
        viewModelScope.launch {
            _state.update {
                it.copy(errorMessage = "App launching will work after BlackBox integration")
            }
        }
    }

    fun showInstallDialog() {
        _state.update { it.copy(showInstallDialog = true) }
    }

    fun dismissInstallDialog() {
        _state.update { it.copy(showInstallDialog = false) }
    }

    fun onVirtualBack() {
        // TODO: Send back event to BlackBox container
    }

    fun onVirtualHome() {
        // TODO: Send home event to BlackBox container
    }

    fun onVirtualRecents() {
        // TODO: Send recents event to BlackBox container
    }

    private fun getDefaultApps(): List<VirtualApp> {
        // Pre-installed system apps that come with the system image
        return listOf(
            VirtualApp("com.android.settings", "Settings"),
            VirtualApp("com.android.calculator2", "Calculator"),
            VirtualApp("com.android.contacts", "Contacts"),
            VirtualApp("com.android.deskclock", "Clock"),
            VirtualApp("com.android.gallery3d", "Gallery"),
            VirtualApp("com.android.camera2", "Camera"),
        )
    }
}
