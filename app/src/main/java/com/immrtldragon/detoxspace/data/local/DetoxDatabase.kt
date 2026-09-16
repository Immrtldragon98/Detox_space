package com.immrtldragon.detoxspace.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InvitationEntity::class], version = 2, exportSchema = true)
abstract class DetoxDatabase : RoomDatabase() {
    abstract fun invitationDao(): InvitationDao
}
