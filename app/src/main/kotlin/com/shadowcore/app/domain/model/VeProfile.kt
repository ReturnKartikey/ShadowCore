package com.shadowcore.app.domain.model

/**
 * Domain model representing a virtual environment profile.
 * This is the pure domain representation — no database annotations.
 */
data class VeProfile(
    val id: String,
    val name: String,
    val androidVersion: AndroidVersion,
    val category: VeCategory,
    val state: VeState = VeState.Stopped,
    val allocatedRamMb: Int = 2048,
    val allocatedStorageMb: Int = 8192,
    val usedStorageMb: Int = 0,
    val usedRamMb: Int = 0,
    val isNetworkIsolated: Boolean = false,
    val hasRootAccess: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0L,
    val snapshotPath: String? = null,
    val executionTier: CapabilityTier = CapabilityTier.TIER_1_CONTAINER,
) {
    val ramUsagePercent: Float
        get() = if (allocatedRamMb > 0) usedRamMb.toFloat() / allocatedRamMb else 0f

    val storageUsagePercent: Float
        get() = if (allocatedStorageMb > 0) usedStorageMb.toFloat() / allocatedStorageMb else 0f

    val isRunning: Boolean get() = state is VeState.Running
    val hasSnapshot: Boolean get() = snapshotPath != null

    val allocatedStorageGb: Float get() = allocatedStorageMb / 1024f
    val usedStorageGb: Float get() = usedStorageMb / 1024f
}
