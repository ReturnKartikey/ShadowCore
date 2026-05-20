package com.shadowcore.app.domain.repository

import com.shadowcore.app.domain.model.VeProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for virtual environment profile data operations.
 * Implemented by data layer — domain layer has no knowledge of Room/database.
 */
interface VeRepository {
    fun getAllVeProfiles(): Flow<List<VeProfile>>
    fun getVeProfileById(id: String): Flow<VeProfile?>
    suspend fun getVeProfileCount(): Int
    suspend fun createVeProfile(profile: VeProfile)
    suspend fun updateVeProfile(profile: VeProfile)
    suspend fun deleteVeProfile(id: String)
    suspend fun deleteAllVeProfiles()
}
