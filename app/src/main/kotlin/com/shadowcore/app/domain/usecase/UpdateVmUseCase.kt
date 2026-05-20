package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.repository.VmRepository
import javax.inject.Inject

/**
 * Updates VM profile settings — RAM, storage, network, root access, etc.
 */
class UpdateVmUseCase @Inject constructor(
    private val vmRepository: VmRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(profile: VmProfile): Result {
        return try {
            vmRepository.updateVmProfile(profile)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update VM")
        }
    }
}
