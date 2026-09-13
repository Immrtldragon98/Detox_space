package com.immrtldragon.detoxspace.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvitationDaoTest {
    private lateinit var database: DetoxDatabase
    private lateinit var dao: InvitationDao

    @Before fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DetoxDatabase::class.java).allowMainThreadQueries().build()
        dao = database.invitationDao()
    }

    @After fun closeDatabase() = database.close()

    @Test fun invitationPersistsAndUpdates() = runTest {
        val row = InvitationEntity(
            id = "invite-1", connectionId = "person-1", connectionName = "Maya",
            signalId = "walk", signalTitle = "Walk?", note = null, timeWindow = "Now",
            state = "SENT", createdAtEpochMillis = 1L, expiresAtEpochMillis = 2L,
        )
        dao.upsert(row)
        assertEquals("SENT", dao.observeAll().first().single().state)
        dao.updateState(row.id, "CANCELLED")
        assertEquals("CANCELLED", dao.findById(row.id)?.state)
    }
}
