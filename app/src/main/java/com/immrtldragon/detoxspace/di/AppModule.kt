package com.immrtldragon.detoxspace.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.immrtldragon.detoxspace.data.DetoxRepository
import com.immrtldragon.detoxspace.data.OfflineFirstDetoxRepository
import com.immrtldragon.detoxspace.data.local.DetoxDatabase
import com.immrtldragon.detoxspace.data.local.InvitationDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindDetoxRepository(implementation: OfflineFirstDetoxRepository): DetoxRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE invitations ADD COLUMN isIncoming INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): DetoxDatabase =
        Room.databaseBuilder(context, DetoxDatabase::class.java, "detox-space.db")
            .addMigrations(migration1To2)
            .build()
    @Provides fun invitationDao(database: DetoxDatabase): InvitationDao = database.invitationDao()
}
