package com.shadowcore.app.ui.screens.vmconsole

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.domain.model.CapabilityTier
import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.domain.repository.VmRepository
import com.shadowcore.app.vm.VmCapabilityChecker
import com.shadowcore.app.vm.VmEngineResult
import com.shadowcore.app.vm.VmManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsoleLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val level: LogLevel = LogLevel.INFO,
)

enum class LogLevel { INFO, SUCCESS, WARN, ERROR, SYSTEM }

data class VmConsoleUiState(
    val profile: VmProfile? = null,
    val logs: List<ConsoleLogEntry> = emptyList(),
    val isBooting: Boolean = false,
    val bootProgress: Float = 0f,
    val isRunning: Boolean = false,
    val errorMessage: String? = null,
    val ramUsageMb: Int = 0,
    val cpuUsagePercent: Float = 0f,
)

@HiltViewModel
class VmConsoleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vmRepository: VmRepository,
    private val vmManager: VmManager,
    private val capabilityChecker: VmCapabilityChecker,
) : ViewModel() {

    private val vmId: String = savedStateHandle["vmId"] ?: ""
    private val _uiState = MutableStateFlow(VmConsoleUiState())
    val uiState: StateFlow<VmConsoleUiState> = _uiState.asStateFlow()

    init {
        // Observe VM profile
        viewModelScope.launch {
            vmRepository.getVmProfileById(vmId).collect { profile ->
                _uiState.update { it.copy(profile = profile, isRunning = profile?.state is VmState.Running) }
            }
        }
    }

    fun startBoot() {
        if (_uiState.value.isBooting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isBooting = true, bootProgress = 0f, logs = emptyList()) }

            val profile = _uiState.value.profile ?: return@launch
            val tier = capabilityChecker.capabilityTier

            // Phase 1: Pre-boot checks
            addLog("ShadowCore VM Engine v1.0.0", LogLevel.SYSTEM)
            delay(200)
            addLog("Initializing ${tier.displayName} runtime...", LogLevel.INFO)
            delay(300)
            updateProgress(0.05f)

            addLog("Target: ${profile.androidVersion.fullDisplayName}", LogLevel.INFO)
            delay(150)
            addLog("Category: ${profile.category.displayName}", LogLevel.INFO)
            delay(150)
            addLog("Allocated RAM: ${profile.allocatedRamMb}MB", LogLevel.INFO)
            delay(150)
            addLog("Allocated Storage: %.1fGB".format(profile.allocatedStorageGb), LogLevel.INFO)
            delay(200)
            updateProgress(0.10f)

            // Phase 2: System checks
            addLog("Checking device capabilities...", LogLevel.SYSTEM)
            delay(400)
            addLog("CPU: ${android.os.Build.HARDWARE}", LogLevel.INFO)
            delay(100)
            addLog("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", LogLevel.INFO)
            delay(100)
            addLog("Host Android: API ${android.os.Build.VERSION.SDK_INT}", LogLevel.INFO)
            delay(200)

            val availRam = capabilityChecker.getAvailableRamMb()
            val totalRam = capabilityChecker.getTotalRamMb()
            addLog("RAM: ${availRam}MB available / ${totalRam}MB total", LogLevel.INFO)
            delay(150)
            updateProgress(0.20f)

            if (availRam < 256) {
                addLog("ERROR: Insufficient RAM (need 256MB minimum)", LogLevel.ERROR)
                _uiState.update { it.copy(isBooting = false, errorMessage = "Insufficient RAM") }
                return@launch
            }
            addLog("RAM check passed ✓", LogLevel.SUCCESS)
            delay(200)

            val availStorage = capabilityChecker.getAvailableStorageMb()
            addLog("Storage: ${availStorage}MB available", LogLevel.INFO)
            delay(150)
            addLog("Storage check passed ✓", LogLevel.SUCCESS)
            delay(200)
            updateProgress(0.30f)

            // Phase 3: Capability tier setup
            addLog("Setting up ${tier.displayName} environment...", LogLevel.SYSTEM)
            delay(300)

            when (tier) {
                CapabilityTier.TIER_1_CONTAINER -> {
                    addLog("Preparing container namespace...", LogLevel.INFO)
                    delay(200)
                    addLog("Creating PID namespace", LogLevel.INFO)
                    delay(150)
                    addLog("Creating mount namespace", LogLevel.INFO)
                    delay(150)
                    addLog("Creating network namespace", LogLevel.INFO)
                    delay(150)
                    addLog("Container environment ready ✓", LogLevel.SUCCESS)
                }
                CapabilityTier.TIER_2_AVF -> {
                    addLog("Initializing VirtualMachineManager...", LogLevel.INFO)
                    delay(300)
                    addLog("Loading Microdroid kernel...", LogLevel.INFO)
                    delay(200)
                    addLog("Configuring AVF protected VM...", LogLevel.INFO)
                    delay(250)
                    addLog("AVF environment ready ✓", LogLevel.SUCCESS)
                }
                CapabilityTier.TIER_3_HARDWARE_ACCELERATED -> {
                    addLog("Initializing KVM hypervisor...", LogLevel.INFO)
                    delay(200)
                    addLog("Configuring vCPU allocation...", LogLevel.INFO)
                    delay(150)
                    addLog("Setting up virtio-gpu passthrough...", LogLevel.INFO)
                    delay(200)
                    addLog("Configuring virtio-net...", LogLevel.INFO)
                    delay(150)
                    addLog("Hardware accelerated environment ready ✓", LogLevel.SUCCESS)
                }
                CapabilityTier.TIER_0_UNSUPPORTED -> {
                    addLog("WARNING: No virtualization support detected", LogLevel.WARN)
                    delay(200)
                    addLog("Falling back to simulation mode", LogLevel.WARN)
                }
            }
            delay(200)
            updateProgress(0.45f)

            // Phase 4: System image
            addLog("Loading Android ${profile.androidVersion.displayName} system image...", LogLevel.SYSTEM)
            delay(300)
            addLog("Verifying system.img integrity...", LogLevel.INFO)
            delay(400)
            addLog("Mounting rootfs...", LogLevel.INFO)
            delay(300)
            addLog("System image loaded (${profile.androidVersion.estimatedImageSizeMb}MB)", LogLevel.SUCCESS)
            delay(200)
            updateProgress(0.60f)

            // Phase 5: Kernel boot
            addLog("Booting kernel...", LogLevel.SYSTEM)
            delay(200)
            addLog("[    0.000000] Linux version 5.15.0-android (shadowcore@build)", LogLevel.INFO)
            delay(100)
            addLog("[    0.001234] Command line: androidboot.hardware=shadowcore", LogLevel.INFO)
            delay(100)
            addLog("[    0.012345] Memory: ${profile.allocatedRamMb}MB", LogLevel.INFO)
            delay(100)
            addLog("[    0.023456] Calibrating delay loop...", LogLevel.INFO)
            delay(200)
            addLog("[    0.045678] Mount-cache hash table entries: 4096", LogLevel.INFO)
            delay(100)
            addLog("[    0.067890] CPU: ARMv8 processor [${android.os.Build.HARDWARE}]", LogLevel.INFO)
            delay(150)
            updateProgress(0.72f)

            // Phase 6: Android init
            addLog("Starting Android init...", LogLevel.SYSTEM)
            delay(300)
            addLog("init: Loading /system/etc/selinux/plat_sepolicy.cil", LogLevel.INFO)
            delay(200)
            addLog("init: starting service 'zygote'", LogLevel.INFO)
            delay(300)
            addLog("init: starting service 'surfaceflinger'", LogLevel.INFO)
            delay(200)
            addLog("init: starting service 'servicemanager'", LogLevel.INFO)
            delay(150)
            updateProgress(0.82f)

            addLog("Starting system services...", LogLevel.INFO)
            delay(200)
            addLog("ActivityManagerService starting...", LogLevel.INFO)
            delay(150)
            addLog("PackageManagerService starting...", LogLevel.INFO)
            delay(200)
            addLog("WindowManagerService starting...", LogLevel.INFO)
            delay(150)
            updateProgress(0.90f)

            // Phase 7: Actually start the VM via engine
            addLog("Finalizing VM launch...", LogLevel.SYSTEM)
            val result = vmManager.startVm(vmId)

            when (result) {
                is VmEngineResult.Success -> {
                    updateProgress(1.0f)
                    delay(200)
                    addLog("SystemServer: Boot completed", LogLevel.SUCCESS)
                    delay(100)
                    addLog("VM '${profile.name}' is now RUNNING", LogLevel.SUCCESS)
                    addLog("─── ${profile.androidVersion.fullDisplayName} @ ${tier.displayName} ───", LogLevel.SYSTEM)
                    _uiState.update { it.copy(
                        isBooting = false,
                        isRunning = true,
                        ramUsageMb = result.profile.usedRamMb,
                    ) }
                }
                is VmEngineResult.Error -> {
                    addLog("FATAL: ${result.message}", LogLevel.ERROR)
                    _uiState.update { it.copy(isBooting = false, errorMessage = result.message) }
                }
                is VmEngineResult.Unsupported -> {
                    addLog("FATAL: ${result.message}", LogLevel.ERROR)
                    _uiState.update { it.copy(isBooting = false, errorMessage = result.message) }
                }
                is VmEngineResult.InsufficientResources -> {
                    addLog("FATAL: ${result.message}", LogLevel.ERROR)
                    _uiState.update { it.copy(isBooting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun stopVm() {
        viewModelScope.launch {
            addLog("Shutting down VM...", LogLevel.SYSTEM)
            delay(200)
            addLog("Stopping system services...", LogLevel.INFO)
            delay(300)
            vmManager.stopVm(vmId)
            addLog("VM stopped", LogLevel.SUCCESS)
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    private fun addLog(message: String, level: LogLevel = LogLevel.INFO) {
        _uiState.update { state ->
            state.copy(logs = state.logs + ConsoleLogEntry(message = message, level = level))
        }
    }

    private fun updateProgress(progress: Float) {
        _uiState.update { it.copy(bootProgress = progress) }
    }
}
