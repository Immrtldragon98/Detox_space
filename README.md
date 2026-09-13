# Detox Space

**Less scrolling. More real moments.**

Detox Space is the native Android successor to Dot Space. It lets trusted people share availability and send tiny, low-pressure invitations such as **Walk?**, **Coffee?**, **Talk?**, and **Free?**. It is not a screen-time blocker, public social network, dating app, or stranger-discovery product.

## V0.2 offline foundation

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
- Repository boundary ready for the existing Dot Space API
- No location, feed, likes, follower counts, or public profiles

Trusted people are still seeded locally for product testing. Invitations and settings persist across app restarts; cloud accounts, synchronization, WebSockets, and FCM belong to the next connected milestone.

## Open in Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Let Android Studio create/use its Gradle wrapper if one is not present.
4. Select an Android 8.0+ emulator or device and run `app`.

## Next milestone

Connect the Android repository to the versioned backend, add encrypted token storage and FCM, then test the core invitation flow on two phones using separate networks.

## Backend foundation

The `backend` workspace contains the Node.js/TypeScript API, PostgreSQL schema, revocable device-session authentication, private connection codes, invitation authorization/state rules, incremental cursor reads, and authenticated Socket.IO transport. Use `docker compose up -d`, copy `backend/.env.example` to `backend/.env`, then run `npm install`, `npm run db:migrate`, and `npm run dev` inside `backend`.

## Planning documents

- [`docs/PRODUCT_SPEC.md`](docs/PRODUCT_SPEC.md) — product rules, user flow, privacy boundaries, and V0 definition of done
- [`docs/ANDROID_ARCHITECTURE.md`](docs/ANDROID_ARCHITECTURE.md) — native stack, modules, synchronization, API boundary, and CI
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — milestones from offline prototype to Play Store rollout
