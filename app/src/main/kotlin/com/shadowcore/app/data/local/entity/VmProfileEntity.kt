package com.shadowcore.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for VM profile persistence.
 * Stores all VM configuration and state data.
 */
@Entity(tableName = "vm_profiles")
data class VmProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val androidVersion: String,   // Enum name: "PIE_9", "R_11", etc.
    val category: String,         // Enum name: "WORK", "GAMING", etc.
    val state: String,            // "STOPPED", "RUNNING", "PAUSED", "ERROR"
    val errorMessage: String? = null,
    val allocatedRamMb: Int = 2048,
    val allocatedStorageMb: Int = 8192,
    val usedStorageMb: Int = 0,
    val usedRamMb: Int = 0,
    val isNetworkIsolated: Boolean = false,
    val hasRootAccess: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0L,
    val snapshotPath: String? = null,
    val executionTier: String = "TIER_1_CONTAINER",
)
