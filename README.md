# Detox Space

**Less scrolling. More real moments.**

> **Android beta:** [Download Detox Space v0.6.0-beta.1 APK](https://github.com/Immrtldragon98/Detox_space/releases/download/v0.6.0-beta.1/Detox-Space-v0.6.0-beta.1.apk) · [Test checklist](docs/BETA_TESTING.md)

Detox Space is the native Android successor to Dot Space. It lets trusted people share availability and send tiny, low-pressure invitations such as **Walk?**, **Coffee?**, **Talk?**, and **Free?**. It is not a screen-time blocker, public social network, dating app, or stranger-discovery product.

## V0.6 minimal beta

- Native Android with Kotlin and Jetpack Compose
- Calm people-first home screen
- Minimal warm-ivory and forest visual system with full dark theme
- Original adaptive launcher icon and legacy Android icon set
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
- Private, single-use 24-hour connection codes
- Connection-code creation, copy, acceptance, and clear failure states
- Immediate trusted-person refresh after a code is accepted
- Automatic access-token renewal with rotating refresh tokens
- Authenticated realtime updates, WorkManager retry, and Firebase push delivery
- API rate limiting, incremental SQL migrations, and dependency security auditing
- Signed, minified Android App Bundle workflow for Play testing
- No location, feed, likes, follower counts, or public profiles

Invitations and settings persist across app restarts. Failed invitation sends remain queued locally and retry on a connected network. WebSockets trigger foreground synchronization and FCM handles background delivery.

## Open in Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Let Android Studio create/use its Gradle wrapper if one is not present.
4. Select an Android 8.0+ emulator or device and run `app`.

Debug builds default to the deployed API at `https://detox-space-api.onrender.com/`. For local emulator development, override it with `-Pdetox.apiBaseUrl=http://10.0.2.2:8080/`; Retrofit requires the trailing slash.

## Delivery layer

- WorkManager retries queued invitations on a connected network and performs a periodic reconciliation.
- Authenticated Socket.IO sends content-free `sync_required` events while the app is open.
- FCM wakes the app with a generic private notification while it is backgrounded or closed.
- Notification payloads never include the person, invitation type, note, or response.

Firebase project `detox-space` is connected to the Android package `com.immrtldragon.detoxspace`. The Render service requires `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, and `FIREBASE_PRIVATE_KEY` as deployment secrets. Never commit the Firebase Admin service-account JSON or its private key.

## Next milestone

Run the complete invitation and push flow on two physical phones over separate networks. Before a public Play Store launch, finish the accessibility review, configure release-signing secrets, capture store assets, and complete closed beta testing.

## Brand assets

The source launcher artwork is stored at [`docs/branding/detox-space-icon-v1.png`](docs/branding/detox-space-icon-v1.png). Android uses an adaptive vector foreground on supported launchers and density-specific PNGs on older devices.

## Backend foundation

The `backend` workspace contains the Node.js/TypeScript API, PostgreSQL schema, revocable device-session authentication, private connection codes, invitation authorization/state rules, incremental cursor reads, and authenticated Socket.IO transport. Use `docker compose up -d`, copy `backend/.env.example` to `backend/.env`, then run `npm install`, `npm run db:migrate`, and `npm run dev` inside `backend`.

## Planning documents

- [`docs/PRODUCT_SPEC.md`](docs/PRODUCT_SPEC.md) — product rules, user flow, privacy boundaries, and V0 definition of done
- [`docs/ANDROID_ARCHITECTURE.md`](docs/ANDROID_ARCHITECTURE.md) — native stack, modules, synchronization, API boundary, and CI
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — milestones from offline prototype to Play Store rollout
- [`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md) — beta privacy notice and data boundaries
- [`docs/BETA_TESTING.md`](docs/BETA_TESTING.md) — two-phone installation and test checklist
