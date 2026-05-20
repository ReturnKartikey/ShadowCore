package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VeState
import com.shadowcore.app.domain.repository.VeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Starts a virtual environment instance — validates state transition and updates profile.
 */
class StartVeUseCase @Inject constructor(
    private val veRepository: VeRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
        data object AlreadyRunning : Result()
    }

    suspend operator fun invoke(veId: String): Result {
        val profile = veRepository.getVeProfileById(veId).first()
            ?: return Result.Error("Virtual environment not found")

        if (!profile.state.canStart) {
            return Result.AlreadyRunning
        }

        return try {
            // Transition to Starting state
            veRepository.updateVeProfile(
                profile.copy(
                    state = VeState.Starting,
                    lastUsedAt = System.currentTimeMillis()
                )
            )

            // Simulate boot -> Running (actual AVF boot would happen in VeEngine)
            veRepository.updateVeProfile(
                profile.copy(
                    state = VeState.Running,
                    lastUsedAt = System.currentTimeMillis()
                )
            )

            Result.Success
        } catch (e: Exception) {
            // On failure, set error state
            veRepository.updateVeProfile(
                profile.copy(state = VeState.Error(e.message ?: "Boot failed"))
            )
            Result.Error(e.message ?: "Failed to start virtual environment")
        }
    }
}
