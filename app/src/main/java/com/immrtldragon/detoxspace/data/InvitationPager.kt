package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.data.remote.ApiInvitation
import com.immrtldragon.detoxspace.data.remote.InvitationsResponse

internal suspend fun syncInvitationPages(
    initialCursor: Long,
    fetch: suspend (Long) -> InvitationsResponse,
    isCurrentSession: () -> Boolean,
    store: suspend (ApiInvitation) -> Unit,
    checkpoint: (Long) -> Unit,
) {
    var cursor = initialCursor
    while (true) {
        val page = fetch(cursor)
        if (!isCurrentSession()) return
        if (page.invitations.isEmpty()) return
        check(page.nextCursor > cursor) { "Invitation cursor did not advance" }
        for (invitation in page.invitations) store(invitation)
        checkpoint(page.nextCursor)
        cursor = page.nextCursor
    }
}
