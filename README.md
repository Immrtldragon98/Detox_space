# Detox Space

**Less scrolling. More real moments.**

Detox Space is the native Android successor to Dot Space. It lets trusted people share availability and send tiny, low-pressure invitations such as **Walk?**, **Coffee?**, **Talk?**, and **Free?**. It is not a screen-time blocker, public social network, dating app, or stranger-discovery product.

## V0.3 connected foundation

- Native Android with Kotlin and Jetpack Compose
- Calm people-first home screen
- Available / Quiet / Away presence
- Trusted-connections list
- Tiny-signal bottom sheet
- Room-persisted invitation history
- Time windows, optional notes, cancellation, completion, and validated lifecycle transitions
- DataStore-persisted dark mode
- Hilt dependency injection
- Unit and Room instrumentation tests
- Register and sign in against the Detox Space API
- Encrypted Android session storage
- Real trusted-connections sync and idempotent invitation sends
- No location, feed, likes, follower counts, or public profiles

Invitations and settings persist across app restarts. Failed invitation sends remain queued locally for the upcoming background-sync milestone. WebSocket UI updates and FCM belong to the next connected milestone.

## Open in Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Let Android Studio create/use its Gradle wrapper if one is not present.
4. Select an Android 8.0+ emulator or device and run `app`.

Debug builds default to `http://10.0.2.2:8080/`, which reaches a backend running on the host from an Android emulator. Override it with `-Pdetox.apiBaseUrl=https://your-api.example/`; Retrofit requires the trailing slash. Use HTTPS for release builds.

## Next milestone

Connect the Android repository to the versioned backend, add encrypted token storage and FCM, then test the core invitation flow on two phones using separate networks.

## Backend foundation

The `backend` workspace contains the Node.js/TypeScript API, PostgreSQL schema, revocable device-session authentication, private connection codes, invitation authorization/state rules, incremental cursor reads, and authenticated Socket.IO transport. Use `docker compose up -d`, copy `backend/.env.example` to `backend/.env`, then run `npm install`, `npm run db:migrate`, and `npm run dev` inside `backend`.

## Planning documents

- [`docs/PRODUCT_SPEC.md`](docs/PRODUCT_SPEC.md) — product rules, user flow, privacy boundaries, and V0 definition of done
- [`docs/ANDROID_ARCHITECTURE.md`](docs/ANDROID_ARCHITECTURE.md) — native stack, modules, synchronization, API boundary, and CI
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — milestones from offline prototype to Play Store rollout
