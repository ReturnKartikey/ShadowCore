package com.shadowcore.app.data.repository

import com.shadowcore.app.data.local.dao.VmProfileDao
import com.shadowcore.app.data.local.entity.VmProfileEntity
import com.shadowcore.app.domain.model.AndroidVersion
import com.shadowcore.app.domain.model.CapabilityTier
import com.shadowcore.app.domain.model.VmCategory
import com.shadowcore.app.domain.model.VmProfile
import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.domain.repository.VmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VmRepository bridging Room entities with domain models.
 * Handles all entity ↔ domain model mapping.
 */
@Singleton
class VmRepositoryImpl @Inject constructor(
    private val vmProfileDao: VmProfileDao,
) : VmRepository {

    override fun getAllVmProfiles(): Flow<List<VmProfile>> {
        return vmProfileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getVmProfileById(id: String): Flow<VmProfile?> {
        return vmProfileDao.getProfileById(id).map { it?.toDomain() }
    }

    override suspend fun getVmProfileCount(): Int {
        return vmProfileDao.getProfileCount()
    }

    override suspend fun createVmProfile(profile: VmProfile) {
        vmProfileDao.insertProfile(profile.toEntity())
    }

    override suspend fun updateVmProfile(profile: VmProfile) {
        vmProfileDao.updateProfile(profile.toEntity())
    }

    override suspend fun deleteVmProfile(id: String) {
        vmProfileDao.deleteProfile(id)
    }

    override suspend fun deleteAllVmProfiles() {
        vmProfileDao.deleteAllProfiles()
    }

    // ── Mapping Functions ──────────────────────────────────────────────

    private fun VmProfileEntity.toDomain(): VmProfile {
        return VmProfile(
            id = id,
            name = name,
            androidVersion = try {
                AndroidVersion.valueOf(androidVersion)
            } catch (_: Exception) {
                AndroidVersion.R_11
            },
            category = try {
                VmCategory.valueOf(category)
            } catch (_: Exception) {
                VmCategory.PERSONAL
            },
            state = when (state) {
                "RUNNING" -> VmState.Running
                "STARTING" -> VmState.Starting
                "PAUSED" -> VmState.Paused
                "DOWNLOADING" -> VmState.Downloading
                "ERROR" -> VmState.Error(errorMessage ?: "Unknown error")
                else -> VmState.Stopped
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

    private fun VmProfile.toEntity(): VmProfileEntity {
        return VmProfileEntity(
            id = id,
            name = name,
            androidVersion = androidVersion.name,
            category = category.name,
            state = when (state) {
                is VmState.Running -> "RUNNING"
                is VmState.Starting -> "STARTING"
                is VmState.Paused -> "PAUSED"
                is VmState.Downloading -> "DOWNLOADING"
                is VmState.Error -> "ERROR"
                is VmState.Stopped -> "STOPPED"
            },
            errorMessage = (state as? VmState.Error)?.message,
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
