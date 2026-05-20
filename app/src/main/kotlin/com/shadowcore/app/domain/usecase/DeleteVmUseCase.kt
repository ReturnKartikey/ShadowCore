package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.repository.VmRepository
import javax.inject.Inject

/**
 * Deletes a VM profile and cleans up associated resources.
 */
class DeleteVmUseCase @Inject constructor(
    private val vmRepository: VmRepository,
) {
    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(vmId: String): Result {
        return try {
            vmRepository.deleteVmProfile(vmId)
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete VM")
        }
    }
}
