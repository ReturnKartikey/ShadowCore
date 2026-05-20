package com.shadowcore.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shadowcore.app.data.local.entity.VeProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for virtual environment profile CRUD operations.
 * All read operations return Flow for reactive UI updates.
 */
@Dao
interface VeProfileDao {

    @Query("SELECT * FROM ve_profiles ORDER BY lastUsedAt DESC")
    fun getAllProfiles(): Flow<List<VeProfileEntity>>

    @Query("SELECT * FROM ve_profiles WHERE id = :id")
    fun getProfileById(id: String): Flow<VeProfileEntity?>

    @Query("SELECT COUNT(*) FROM ve_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VeProfileEntity)

    @Update
    suspend fun updateProfile(profile: VeProfileEntity)

    @Query("DELETE FROM ve_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("DELETE FROM ve_profiles")
    suspend fun deleteAllProfiles()

    @Query("SELECT * FROM ve_profiles WHERE state = 'RUNNING' OR state = 'PAUSED' OR state = 'STARTING'")
    fun getActiveProfiles(): Flow<List<VeProfileEntity>>

    @Query("UPDATE ve_profiles SET state = 'STOPPED', usedRamMb = 0 WHERE state = 'RUNNING' OR state = 'STARTING'")
    suspend fun stopAllRunningProfiles()
}
