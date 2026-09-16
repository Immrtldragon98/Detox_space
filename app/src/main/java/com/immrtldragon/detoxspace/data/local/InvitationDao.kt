package com.immrtldragon.detoxspace.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InvitationDao {
    @Query("SELECT * FROM invitations ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<InvitationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(invitation: InvitationEntity)

    @Query("SELECT * FROM invitations WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): InvitationEntity?

    @Query("UPDATE invitations SET state = :state WHERE id = :id")
    suspend fun updateState(id: String, state: String)

    @Query("SELECT * FROM invitations WHERE state = 'SENDING' AND expiresAtEpochMillis > :now")
    suspend fun pending(now: Long): List<InvitationEntity>

    @Query("DELETE FROM invitations")
    suspend fun clear()
}
