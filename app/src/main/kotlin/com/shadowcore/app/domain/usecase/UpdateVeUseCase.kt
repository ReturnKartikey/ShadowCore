package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.repository.VeRepository
import javax.inject.Inject

/**
 * Updates virtual environment profile settings — RAM, storage, network, root access, etc.
 */
class UpdateVeUseCase @Inject constructor(
    private val veRepository: VeRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(profile: VeProfile): Result {
        return try {
            veRepository.updateVeProfile(profile)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update virtual environment")
        }
    }
}
