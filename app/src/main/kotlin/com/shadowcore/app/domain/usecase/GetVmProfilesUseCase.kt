package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.repository.VmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Retrieves all VM profiles as a reactive Flow.
 * Profiles are sorted by last used time (most recent first).
 */
class GetVmProfilesUseCase @Inject constructor(
    private val vmRepository: VmRepository,
) {
    operator fun invoke(): Flow<List<VmProfile>> {
        return vmRepository.getAllVmProfiles()
    }
}
