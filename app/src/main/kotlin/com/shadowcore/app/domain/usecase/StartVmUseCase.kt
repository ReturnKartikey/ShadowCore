package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.domain.repository.VmRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Starts a VM instance — validates state transition and updates profile.
 */
class StartVmUseCase @Inject constructor(
    private val vmRepository: VmRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
        data object AlreadyRunning : Result()
    }

    suspend operator fun invoke(vmId: String): Result {
        val profile = vmRepository.getVmProfileById(vmId).first()
            ?: return Result.Error("VM not found")

        if (!profile.state.canStart) {
            return Result.AlreadyRunning
        }

        return try {
            // Transition to Starting state
            vmRepository.updateVmProfile(
                profile.copy(
                    state = VmState.Starting,
                    lastUsedAt = System.currentTimeMillis()
                )
            )

            // Simulate boot -> Running (actual AVF boot would happen in VmEngine)
            vmRepository.updateVmProfile(
                profile.copy(
                    state = VmState.Running,
                    lastUsedAt = System.currentTimeMillis()
                )
            )

            Result.Success
        } catch (e: Exception) {
            // On failure, set error state
            vmRepository.updateVmProfile(
                profile.copy(state = VmState.Error(e.message ?: "Boot failed"))
            )
            Result.Error(e.message ?: "Failed to start VM")
        }
    }
}
