package com.shadowcore.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shadowcore.app.data.local.dao.VeProfileDao
import com.shadowcore.app.data.local.entity.VeProfileEntity

/**
 * Room database for ShadowCore.
 * Stores virtual environment profiles and settings.
 */
@Database(
    entities = [VeProfileEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class ShadowCoreDatabase : RoomDatabase() {
    abstract fun veProfileDao(): VeProfileDao

    companion object {
        const val DATABASE_NAME = "shadowcore_db"
    }
}

