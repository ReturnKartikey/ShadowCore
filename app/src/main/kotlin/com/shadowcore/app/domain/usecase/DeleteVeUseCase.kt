package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.repository.VeRepository
import javax.inject.Inject

/**
 * Deletes a virtual environment profile and cleans up associated resources.
 */
class DeleteVeUseCase @Inject constructor(
    private val veRepository: VeRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(veId: String): Result {
        return try {
            veRepository.deleteVeProfile(veId)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete virtual environment")
        }
    }
}
