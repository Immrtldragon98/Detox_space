# Detox Space — Android Architecture

## 1. Selected stack

| Concern | Selection |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | UI, domain, and data layers with unidirectional data flow |
| State | ViewModel + StateFlow |
| Navigation | Navigation Compose |
| Local database | Room |
| Preferences | DataStore |
| Dependency injection | Hilt |
| REST | Retrofit + OkHttp |
| Realtime | Authenticated WebSocket |
| Serialization | Kotlin Serialization |
| Background work | WorkManager |
| Push | Firebase Cloud Messaging |
| Secret storage | Android Keystore-backed encrypted storage |
| Images | None required for V0 core flow |
| Tests | JUnit, coroutine tests, Room tests, Compose UI tests |

## 2. Modules

Start with a small modular structure. Split further only when the code and team justify it.

```text
app
core:model
core:designsystem
core:database
core:network
core:notifications
feature:auth
feature:people
feature:invitations
feature:connections
feature:settings
```

The current single `app` module is a product prototype. V0.2 introduces the core modules before cloud integration.

## 3. Data flow

```text
Compose screen
  -> UI event
  -> ViewModel
  -> use case
  -> repository
  -> Room and/or remote data source
  -> immutable Flow
  -> ViewModel UI state
  -> Compose screen
```

Room is the Android source of truth. Network responses update Room; screens observe Room. This provides stable offline behaviour and avoids different screens holding conflicting copies of data.

## 4. Offline and synchronization rules

- Render cached people, presence, and invitation history immediately.
- Queue a newly created invitation locally with state `SENDING`.
- Assign a client-generated UUID so retries cannot create duplicates.
- Send through the API when connectivity is available.
- Replace local state with the server-authoritative version.
- Reconnect WebSocket with exponential backoff and jitter.
- Fetch changes since the last server cursor after reconnecting.
- Use WorkManager for durable retry, not for active realtime presence.
- Treat presence as short-lived server data with an explicit expiry.

## 5. Backend compatibility

The original Dot Space backend provides useful concepts but requires a contract review before direct reuse.

Preserve:

- users and authentication
- accepted connections
- per-connection signal permission
- server-side device sessions and revocation
- rate limits
- Redis-backed realtime delivery
- push fallback for background/offline recipients

Change:

- rename generic signals to time-bound invitations
- add response and lifecycle states
- add proposed time and expiry
- add idempotency keys
- add incremental synchronization cursor
- replace Expo push tokens with FCM registration tokens
- add deletion, block, and security-audit flows

## 6. API boundary

Proposed endpoints:

```text
POST   /v1/auth/register
POST   /v1/auth/login
POST   /v1/auth/refresh
POST   /v1/auth/logout

GET    /v1/devices
DELETE /v1/devices/{deviceId}
PUT    /v1/devices/{deviceId}/push-token

GET    /v1/connections
POST   /v1/connections/invites
POST   /v1/connections/invites/{code}/accept
PATCH  /v1/connections/{connectionId}/permissions
DELETE /v1/connections/{connectionId}
POST   /v1/connections/{connectionId}/block

PUT    /v1/presence
DELETE /v1/presence

GET    /v1/invitations?cursor={cursor}
POST   /v1/invitations
POST   /v1/invitations/{invitationId}/respond
POST   /v1/invitations/{invitationId}/cancel
POST   /v1/invitations/{invitationId}/complete

GET    /v1/sync?cursor={cursor}
```

## 7. Delivery guarantees

- Creating an invitation uses a client UUID as an idempotency key.
- The API returns the existing invitation when the same key is retried.
- WebSocket events contain monotonically ordered per-user cursors.
- FCM is a wake-up/delivery hint, not the source of truth.
- Opening a push notification triggers an authenticated API synchronization.
- The database remains authoritative for invitation state.

## 8. Environments

| Environment | Purpose |
|---|---|
| local | Emulator/device plus Docker backend |
| test | Automated integration and UI tests |
| staging | Closed beta and migration verification |
| production | Play Store users |

Each environment must use separate API URLs, databases, Firebase projects, signing configuration, and secrets.

## 9. CI checks

Every pull request should run:

- Gradle dependency verification
- Kotlin compilation
- Android lint
- formatting/static analysis
- unit tests
- Compose UI tests where supported
- backend type checking and tests
- migration tests
- debug APK build
- secret scanning

Release tags additionally build a signed AAB using protected CI secrets.

