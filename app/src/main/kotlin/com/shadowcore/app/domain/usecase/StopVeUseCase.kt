package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VeState
import com.shadowcore.app.domain.repository.VeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Stops a running virtual environment instance — saves state and transitions to Stopped.
 */
class StopVeUseCase @Inject constructor(
    private val veRepository: VeRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
        data object NotRunning : Result()
    }

    suspend operator fun invoke(veId: String): Result {
        val profile = veRepository.getVeProfileById(veId).first()
            ?: return Result.Error("Virtual environment not found")

        if (!profile.state.canStop) {
            return Result.NotRunning
        }

        return try {
            veRepository.updateVeProfile(
                profile.copy(
                    state = VeState.Stopped,
                    lastUsedAt = System.currentTimeMillis(),
                    usedRamMb = 0
                )
            )
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to stop virtual environment")
        }
    }
}
