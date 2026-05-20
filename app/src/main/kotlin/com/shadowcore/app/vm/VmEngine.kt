package com.shadowcore.app.vm

import android.util.Log
import com.shadowcore.app.domain.model.CapabilityTier
import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.model.VmState
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VmEngine @Inject constructor(
    private val capabilityChecker: VmCapabilityChecker,
) {
    companion object {
        private const val TAG = "VmEngine"
    }

    suspend fun bootVm(profile: VmProfile): VmEngineResult {
        val tier = capabilityChecker.capabilityTier
        Log.i(TAG, "Booting VM '${profile.name}' at tier: ${tier.displayName}")

        if (!tier.isSupported) {
            return VmEngineResult.Unsupported("Device does not support virtualization")
        }

        // Relaxed RAM check — allow boot if at least 256MB is free
        // The VM will use what it can within its allocation
        val availableRam = capabilityChecker.getAvailableRamMb()
        if (availableRam < 256) {
            return VmEngineResult.InsufficientResources(
                "Not enough free RAM. Only ${availableRam}MB available, need at least 256MB."
            )
        }

        return try {
            when (tier) {
                CapabilityTier.TIER_1_CONTAINER -> bootContainer(profile)
                CapabilityTier.TIER_2_AVF -> bootAvf(profile)
                CapabilityTier.TIER_3_HARDWARE_ACCELERATED -> bootAccelerated(profile)
                CapabilityTier.TIER_0_UNSUPPORTED -> VmEngineResult.Unsupported("Not supported")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Boot failed", e)
            VmEngineResult.Error("Boot failed: ${e.message}")
        }
    }

    suspend fun stopVm(profile: VmProfile): VmEngineResult {
        return try {
            Log.i(TAG, "Stopping VM '${profile.name}'")
            delay(400)
            VmEngineResult.Success(profile.copy(state = VmState.Stopped, usedRamMb = 0))
        } catch (e: Exception) {
            VmEngineResult.Error("Failed to stop: ${e.message}")
        }
    }

    suspend fun pauseVm(profile: VmProfile): VmEngineResult {
        if (!capabilityChecker.capabilityTier.supportsSnapshot) {
            return VmEngineResult.Error("Snapshots not supported at this tier")
        }
        return try {
            Log.i(TAG, "Pausing VM '${profile.name}'")
            delay(200)
            VmEngineResult.Success(profile.copy(state = VmState.Paused))
        } catch (e: Exception) {
            VmEngineResult.Error("Failed to pause: ${e.message}")
        }
    }

    suspend fun repairVm(profile: VmProfile): VmEngineResult {
        return try {
            Log.i(TAG, "Repairing VM '${profile.name}'")
            delay(500)
            VmEngineResult.Success(
                profile.copy(state = VmState.Stopped, usedRamMb = 0, snapshotPath = null)
            )
        } catch (e: Exception) {
            VmEngineResult.Error("Repair failed: ${e.message}")
        }
    }

    private suspend fun bootContainer(profile: VmProfile): VmEngineResult {
        Log.i(TAG, "Container boot for '${profile.name}'")
        delay(800)
        val availRam = capabilityChecker.getAvailableRamMb()
        val usedRam = minOf((profile.allocatedRamMb * 0.3).toInt(), availRam / 2)
        return VmEngineResult.Success(profile.copy(
            state = VmState.Running, usedRamMb = usedRam,
            lastUsedAt = System.currentTimeMillis(),
            executionTier = CapabilityTier.TIER_1_CONTAINER,
        ))
    }

    private suspend fun bootAvf(profile: VmProfile): VmEngineResult {
        Log.i(TAG, "AVF boot for '${profile.name}'")
        delay(1200)
        val availRam = capabilityChecker.getAvailableRamMb()
        val usedRam = minOf((profile.allocatedRamMb * 0.5).toInt(), availRam / 2)
        return VmEngineResult.Success(profile.copy(
            state = VmState.Running, usedRamMb = usedRam,
            lastUsedAt = System.currentTimeMillis(),
            executionTier = CapabilityTier.TIER_2_AVF,
        ))
    }

    private suspend fun bootAccelerated(profile: VmProfile): VmEngineResult {
        Log.i(TAG, "HW Accelerated boot for '${profile.name}'")
        delay(800)
        val availRam = capabilityChecker.getAvailableRamMb()
        val usedRam = minOf((profile.allocatedRamMb * 0.6).toInt(), availRam / 2)
        return VmEngineResult.Success(profile.copy(
            state = VmState.Running, usedRamMb = usedRam,
            lastUsedAt = System.currentTimeMillis(),
            executionTier = CapabilityTier.TIER_3_HARDWARE_ACCELERATED,
        ))
    }
}

sealed class VmEngineResult {
    data class Success(val profile: VmProfile) : VmEngineResult()
    data class Error(val message: String) : VmEngineResult()
    data class Unsupported(val message: String) : VmEngineResult()
    data class InsufficientResources(val message: String) : VmEngineResult()
}
