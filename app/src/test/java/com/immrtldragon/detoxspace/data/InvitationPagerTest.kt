package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.data.remote.ApiInvitation
import com.immrtldragon.detoxspace.data.remote.InvitationsResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class InvitationPagerTest {
    private fun row(id: Int) = ApiInvitation("$id", "sender", "recipient", "WALK", null,
        "2026-11-27T00:00:00Z", "2026-11-28T00:00:00Z", "SENT", id.toLong())

    @Test fun loadsMoreThanTwoHundredAndResumes() = runTest {
        val rows = (1..451).map(::row)
        val stored = mutableListOf<String>()
        var cursor = 0L
        val fetches = mutableListOf<Long>()
        val fetch: suspend (Long) -> InvitationsResponse = { start ->
            fetches.add(start)
            val page = rows.filter { it.sequence_id > start }.take(200)
            InvitationsResponse(page, page.lastOrNull()?.sequence_id ?: start)
        }
        syncInvitationPages(cursor, fetch, { true }, { stored.add(it.id) }, { cursor = it })
        assertEquals(451, stored.size)
        assertEquals(listOf(0L, 200L, 400L, 451L), fetches)
        syncInvitationPages(cursor, fetch, { true }, { fail("Should not replay stored pages") }, {})
        assertEquals(451L, fetches.last())
    }

    @Test fun failedPageDoesNotAdvanceCheckpoint() = runTest {
        var cursor = 0L
        try {
            syncInvitationPages(0, { InvitationsResponse(listOf(row(1), row(2)), 2) },
                { true }, { if (it.id == "2") error("disk failure") }, { cursor = it })
            fail("Expected failure")
        } catch (_: IllegalStateException) { }
        assertEquals(0L, cursor)
    }

    @Test fun changedSessionDiscardsInFlightPage() = runTest {
        syncInvitationPages(0, { InvitationsResponse(listOf(row(1)), 1) }, { false },
            { fail("Old session must not write") }, { fail("Old session must not checkpoint") })
    }

    @Test fun rejectsNonAdvancingCursor() = runTest {
        try {
            syncInvitationPages(1, { InvitationsResponse(listOf(row(1)), 1) }, { true }, {}, {})
            fail("Expected cursor error")
        } catch (_: IllegalStateException) { }
    }
}
