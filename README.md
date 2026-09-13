# Detox Space

**Less scrolling. More real moments.**

Detox Space is the native Android successor to Dot Space. It lets trusted people share availability and send tiny, low-pressure invitations such as **Walk?**, **Coffee?**, **Talk?**, and **Free?**. It is not a screen-time blocker, public social network, dating app, or stranger-discovery product.

## V0.1 foundation

- Native Android with Kotlin and Jetpack Compose
- Calm people-first home screen
- Available / Quiet / Away presence
- Trusted-connections list
- Tiny-signal bottom sheet
- Local prototype signal history
- Repository boundary ready for the existing Dot Space API
- No location, feed, likes, follower counts, or public profiles

The current data source is intentionally in-memory so the product interaction can be validated before wiring authentication, persistence, WebSockets, and FCM.

## Open in Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Let Android Studio create/use its Gradle wrapper if one is not present.
4. Select an Android 8.0+ emulator or device and run `app`.

## Next milestone

Replace `InMemoryDetoxRepository` with Room-backed local storage, then add a remote implementation compatible with the original Dot Space REST and realtime contracts.

