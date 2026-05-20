package com.shadowcore.app.vm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.domain.repository.VmRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level VM lifecycle manager coordinating VmEngine, VmRepository, and VmService.
 */
@Singleton
class VmManager @Inject constructor(
    private val vmEngine: VmEngine,
    private val vmRepository: VmRepository,
    private val capabilityChecker: VmCapabilityChecker,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "VmManager"
    }

    suspend fun startVm(vmId: String): VmEngineResult {
        val profile = vmRepository.getVmProfileById(vmId).first()
            ?: return VmEngineResult.Error("VM not found")

        if (!profile.state.canStart) {
            return VmEngineResult.Error("VM cannot start from state: ${profile.state.displayName}")
        }

        // Check simultaneous VM limit for current tier
        val tier = capabilityChecker.capabilityTier
        val activeVms = vmRepository.getAllVmProfiles().first().count { it.state.isActive }
        if (activeVms >= tier.maxSimultaneousVms) {
            return VmEngineResult.Error(
                "Max ${tier.maxSimultaneousVms} simultaneous VMs for ${tier.displayName}"
            )
        }

        // Update to starting state
        vmRepository.updateVmProfile(profile.copy(state = VmState.Starting))

        // Boot via engine
        val result = vmEngine.bootVm(profile)
        when (result) {
            is VmEngineResult.Success -> {
                vmRepository.updateVmProfile(result.profile)
                // Start foreground service to keep VM alive
                startVmService()
            }
            is VmEngineResult.Error -> vmRepository.updateVmProfile(
                profile.copy(state = VmState.Error(result.message))
            )
            is VmEngineResult.Unsupported -> vmRepository.updateVmProfile(
                profile.copy(state = VmState.Error(result.message))
            )
            is VmEngineResult.InsufficientResources -> vmRepository.updateVmProfile(
                profile.copy(state = VmState.Error(result.message))
            )
        }
        return result
    }

    suspend fun stopVm(vmId: String): VmEngineResult {
        val profile = vmRepository.getVmProfileById(vmId).first()
            ?: return VmEngineResult.Error("VM not found")

        val result = vmEngine.stopVm(profile)
        if (result is VmEngineResult.Success) {
            vmRepository.updateVmProfile(result.profile)
            // Stop service if no more active VMs
            val remaining = vmRepository.getAllVmProfiles().first().count {
                it.state.isActive && it.id != vmId
            }
            if (remaining == 0) stopVmService()
        }
        return result
    }

    suspend fun pauseVm(vmId: String): VmEngineResult {
        val profile = vmRepository.getVmProfileById(vmId).first()
            ?: return VmEngineResult.Error("VM not found")

        val result = vmEngine.pauseVm(profile)
        if (result is VmEngineResult.Success) {
            vmRepository.updateVmProfile(result.profile)
        }
        return result
    }

    suspend fun repairVm(vmId: String): VmEngineResult {
        val profile = vmRepository.getVmProfileById(vmId).first()
            ?: return VmEngineResult.Error("VM not found")

        val result = vmEngine.repairVm(profile)
        if (result is VmEngineResult.Success) {
            vmRepository.updateVmProfile(result.profile)
        }
        return result
    }

    /**
     * Auto-recovery: stop any VMs that were left in Running/Starting state
     * from a previous app session (crash recovery).
     */
    suspend fun recoverFromCrash() {
        Log.i(TAG, "Checking for crashed VMs...")
        val allProfiles = vmRepository.getAllVmProfiles().first()
        allProfiles.filter { it.state.isActive }.forEach { profile ->
            Log.w(TAG, "Recovering crashed VM: ${profile.name}")
            vmRepository.updateVmProfile(
                profile.copy(state = VmState.Stopped, usedRamMb = 0)
            )
        }
    }

    private fun startVmService() {
        try {
            val activeCount = 1 // Will be updated by service
            val intent = Intent(context, VmService::class.java).apply {
                putExtra("vm_count", activeCount)
            }
            ContextCompat.startForegroundService(context, intent)
            Log.i(TAG, "VmService started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VmService", e)
        }
    }

    private fun stopVmService() {
        try {
            context.stopService(Intent(context, VmService::class.java))
            Log.i(TAG, "VmService stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop VmService", e)
        }
    }

    /**
     * Check if notification permission is granted (needed for foreground service on Android 13+).
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
