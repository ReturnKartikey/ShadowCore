package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.repository.VeRepository
import javax.inject.Inject

/**
 * Creates a new virtual environment profile with validation.
 * No premium gating — all features available to all users.
 */
class CreateVeUseCase @Inject constructor(
    private val veRepository: VeRepository,
) {
    sealed class Result {
        data class Success(val profile: VeProfile) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(profile: VeProfile): Result {
        // Validate profile
        if (profile.name.isBlank()) {
            return Result.Error("Virtual environment name cannot be empty")
        }

        if (profile.allocatedRamMb < MIN_RAM_MB) {
            return Result.Error("Minimum RAM is ${MIN_RAM_MB}MB")
        }

        val currentCount = veRepository.getVeProfileCount()
        if (currentCount >= MAX_VE_LIMIT) {
            return Result.Error("Maximum $MAX_VE_LIMIT virtual environments reached")
        }

        return try {
            veRepository.createVeProfile(profile)
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create virtual environment")
        }
    }

    companion object {
        const val MAX_VE_LIMIT = 20
        const val MIN_RAM_MB = 512
    }
}
