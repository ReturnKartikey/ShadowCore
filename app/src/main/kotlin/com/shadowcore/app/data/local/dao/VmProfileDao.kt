package com.shadowcore.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shadowcore.app.data.local.entity.VmProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for VM profile CRUD operations.
 * All read operations return Flow for reactive UI updates.
 */
@Dao
interface VmProfileDao {

    @Query("SELECT * FROM vm_profiles ORDER BY lastUsedAt DESC")
    fun getAllProfiles(): Flow<List<VmProfileEntity>>

    @Query("SELECT * FROM vm_profiles WHERE id = :id")
    fun getProfileById(id: String): Flow<VmProfileEntity?>

    @Query("SELECT COUNT(*) FROM vm_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VmProfileEntity)

    @Update
    suspend fun updateProfile(profile: VmProfileEntity)

    @Query("DELETE FROM vm_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("DELETE FROM vm_profiles")
    suspend fun deleteAllProfiles()

    @Query("SELECT * FROM vm_profiles WHERE state = 'RUNNING' OR state = 'PAUSED' OR state = 'STARTING'")
    fun getActiveProfiles(): Flow<List<VmProfileEntity>>

    @Query("UPDATE vm_profiles SET state = 'STOPPED', usedRamMb = 0 WHERE state = 'RUNNING' OR state = 'STARTING'")
    suspend fun stopAllRunningProfiles()
}
