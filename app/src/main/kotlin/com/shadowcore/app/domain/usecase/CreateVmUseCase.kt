package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.repository.VmRepository
import javax.inject.Inject

/**
 * Creates a new VM profile with validation.
 * No premium gating — all features available to all users.
 */
class CreateVmUseCase @Inject constructor(
    private val vmRepository: VmRepository,
) {
    sealed class Result {
        data class Success(val profile: VmProfile) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(profile: VmProfile): Result {
        // Validate profile
        if (profile.name.isBlank()) {
            return Result.Error("VM name cannot be empty")
        }

        if (profile.allocatedRamMb < MIN_RAM_MB) {
            return Result.Error("Minimum RAM is ${MIN_RAM_MB}MB")
        }

        val currentCount = vmRepository.getVmProfileCount()
        if (currentCount >= MAX_VM_LIMIT) {
            return Result.Error("Maximum $MAX_VM_LIMIT VMs reached")
        }

        return try {
            vmRepository.createVmProfile(profile)
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create VM")
        }
    }

    companion object {
        const val MAX_VM_LIMIT = 20
        const val MIN_RAM_MB = 512
    }
}
