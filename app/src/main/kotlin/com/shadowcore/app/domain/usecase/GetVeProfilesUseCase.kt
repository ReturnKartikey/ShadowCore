package com.shadowcore.app.domain.usecase

import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.repository.VeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Retrieves all virtual environment profiles as a reactive Flow.
 * Profiles are sorted by last used time (most recent first).
 */
class GetVeProfilesUseCase @Inject constructor(
    private val veRepository: VeRepository,
) {
    operator fun invoke(): Flow<List<VeProfile>> {
        return veRepository.getAllVeProfiles()
    }
}
