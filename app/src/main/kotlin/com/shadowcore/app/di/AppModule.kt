package com.shadowcore.app.di

import android.content.Context
import androidx.room.Room
import com.shadowcore.app.data.local.ShadowCoreDatabase
import com.shadowcore.app.data.local.dao.VeProfileDao
import com.shadowcore.app.data.repository.BillingRepositoryImpl
import com.shadowcore.app.data.repository.VeRepositoryImpl
import com.shadowcore.app.domain.repository.BillingRepository
import com.shadowcore.app.domain.repository.VeRepository
import com.shadowcore.app.engine.ContainerEngine
import com.shadowcore.app.engine.StubContainerEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShadowCoreDatabase {
        return Room.databaseBuilder(
            context,
            ShadowCoreDatabase::class.java,
            ShadowCoreDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideVeProfileDao(db: ShadowCoreDatabase): VeProfileDao {
        return db.veProfileDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVeRepository(impl: VeRepositoryImpl): VeRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    @Binds
    @Singleton
    abstract fun bindContainerEngine(impl: StubContainerEngine): ContainerEngine
}
