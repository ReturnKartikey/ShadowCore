package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.domain.repository.VmRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Stops a running VM instance — saves state and transitions to Stopped.
 */
class StopVmUseCase @Inject constructor(
    private val vmRepository: VmRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
        data object NotRunning : Result()
    }

    suspend operator fun invoke(vmId: String): Result {
        val profile = vmRepository.getVmProfileById(vmId).first()
            ?: return Result.Error("VM not found")

        if (!profile.state.canStop) {
            return Result.NotRunning
        }

        return try {
            vmRepository.updateVmProfile(
                profile.copy(
                    state = VmState.Stopped,
                    lastUsedAt = System.currentTimeMillis(),
                    usedRamMb = 0
                )
            )
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to stop VM")
        }
    }
}
