package com.shadowcore.app.data.repository

import com.shadowcore.app.data.local.dao.VeProfileDao
import com.shadowcore.app.data.local.entity.VeProfileEntity
import com.shadowcore.app.domain.model.AndroidVersion
import com.shadowcore.app.domain.model.CapabilityTier
import com.shadowcore.app.domain.model.VeCategory
import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.model.VeState
import com.shadowcore.app.domain.repository.VeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VeRepository bridging Room entities with domain models.
 * Handles all entity ↔ domain model mapping.
 */
@Singleton
class VeRepositoryImpl @Inject constructor(
    private val veProfileDao: VeProfileDao,
) : VeRepository {

    override fun getAllVeProfiles(): Flow<List<VeProfile>> {
        return veProfileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVeProfileById(id: String): Flow<VeProfile?> {
        return veProfileDao.getProfileById(id).map { it?.toDomain() }
    }

    override suspend fun getVeProfileCount(): Int {
        return veProfileDao.getProfileCount()
    }

    override suspend fun createVeProfile(profile: VeProfile) {
        veProfileDao.insertProfile(profile.toEntity())
    }

    override suspend fun updateVeProfile(profile: VeProfile) {
        veProfileDao.updateProfile(profile.toEntity())
    }

    override suspend fun deleteVeProfile(id: String) {
        veProfileDao.deleteProfile(id)
    }

    override suspend fun deleteAllVeProfiles() {
        veProfileDao.deleteAllProfiles()
    }

    // ── Mapping Functions ──────────────────────────────────────────────

    private fun VeProfileEntity.toDomain(): VeProfile {
        return VeProfile(
            id = id,
            name = name,
            androidVersion = try {
                AndroidVersion.valueOf(androidVersion)
            } catch (_: Exception) {
                AndroidVersion.R_11
            },
            category = try {
                VeCategory.valueOf(category)
            } catch (_: Exception) {
                VeCategory.PERSONAL
            },
            state = when (state) {
                "RUNNING" -> VeState.Running
                "STARTING" -> VeState.Starting
                "PAUSED" -> VeState.Paused
                "DOWNLOADING" -> VeState.Downloading
                "ERROR" -> VeState.Error(errorMessage ?: "Unknown error")
                else -> VeState.Stopped
            },
            allocatedRamMb = allocatedRamMb,
            allocatedStorageMb = allocatedStorageMb,
            usedStorageMb = usedStorageMb,
            usedRamMb = usedRamMb,
            isNetworkIsolated = isNetworkIsolated,
            hasRootAccess = hasRootAccess,
            createdAt = createdAt,
            lastUsedAt = lastUsedAt,
            snapshotPath = snapshotPath,
            executionTier = try {
                CapabilityTier.valueOf(executionTier)
            } catch (_: Exception) {
                CapabilityTier.TIER_1_CONTAINER
            },
        )
    }

    private fun VeProfile.toEntity(): VeProfileEntity {
        return VeProfileEntity(
            id = id,
            name = name,
            androidVersion = androidVersion.name,
            category = category.name,
            state = when (state) {
                is VeState.Running -> "RUNNING"
                is VeState.Starting -> "STARTING"
                is VeState.Paused -> "PAUSED"
                is VeState.Downloading -> "DOWNLOADING"
                is VeState.Error -> "ERROR"
                is VeState.Stopped -> "STOPPED"
            },
            errorMessage = (state as? VeState.Error)?.message,
            allocatedRamMb = allocatedRamMb,
            allocatedStorageMb = allocatedStorageMb,
            usedStorageMb = usedStorageMb,
            usedRamMb = usedRamMb,
            isNetworkIsolated = isNetworkIsolated,
            hasRootAccess = hasRootAccess,
            createdAt = createdAt,
            lastUsedAt = lastUsedAt,
            snapshotPath = snapshotPath,
            executionTier = executionTier.name,
        )
    }
}
