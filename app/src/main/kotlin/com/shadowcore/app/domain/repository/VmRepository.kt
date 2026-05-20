package com.shadowcore.app.domain.repository

import com.shadowcore.app.domain.model.VmProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for VM profile data operations.
 * Implemented by data layer — domain layer has no knowledge of Room/database.
 */
interface VmRepository {
    fun getAllVmProfiles(): Flow<List<VmProfile>>
    fun getVmProfileById(id: String): Flow<VmProfile?>
    suspend fun getVmProfileCount(): Int
    suspend fun createVmProfile(profile: VmProfile)
    suspend fun updateVmProfile(profile: VmProfile)
    suspend fun deleteVmProfile(id: String)
    suspend fun deleteAllVmProfiles()
}
