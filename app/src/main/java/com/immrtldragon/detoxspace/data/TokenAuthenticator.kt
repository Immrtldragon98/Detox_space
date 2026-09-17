package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.data.remote.RefreshApi
import com.immrtldragon.detoxspace.data.remote.RefreshRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val refreshApi: RefreshApi,
    private val sessions: SessionStore,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        synchronized(this) {
            val current = sessions.current() ?: return null
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (requestToken != current.accessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${current.accessToken}")
                    .build()
            }
            val refreshed = runCatching {
                refreshApi.refresh(RefreshRequest(current.refreshToken)).execute()
            }.getOrNull()
            val body = refreshed?.takeIf { it.isSuccessful }?.body()
            if (body == null) {
                sessions.clear()
                return null
            }
            sessions.save(Session(body.accessToken, body.refreshToken, body.userId, body.sessionId))
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${body.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
