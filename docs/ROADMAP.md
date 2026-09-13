# Detox Space — Delivery Roadmap

## Milestone 0 — Product contract

Deliverables:

- approved product specification
- invitation state machine
- non-goals and notification policy
- architecture and data ownership decisions

Done when new feature proposals can be accepted or rejected using the specification rather than personal preference.

## Milestone 1 — Reliable offline Android foundation

Deliverables:

- Gradle Wrapper and reproducible build
- navigation and screen-level UI state
- Room entities and migrations
- DataStore preferences
- Hilt dependency graph
- local invitation creation and response
- dark mode, empty states, errors, and accessibility semantics
- unit and Compose UI tests

Done when the complete invitation flow works after process death and without a network.

## Milestone 2 — Backend contract upgrade

Deliverables:

- versioned `/v1` API
- invitation lifecycle schema
- idempotent mutation endpoints
- block and permission enforcement
- incremental sync cursor
- PostgreSQL migration tests
- local Docker development environment

Done when two API clients can exchange and resolve invitations consistently.

## Milestone 3 — Connected Android application

Deliverables:

- authentication and refresh-token rotation
- secure device identity
- REST synchronization
- authenticated WebSocket client
- offline mutation queue
- FCM token registration and notification routing

Done when two real phones on separate networks complete the core flow with foreground, background, and killed-app delivery.

## Milestone 4 — Trust and closed beta

Deliverables:

- connection invite links/codes
- remove, block, mute, and report
- device list and remote revocation
- account deletion
- privacy policy
- rate-limit and abuse tests
- Crashlytics and privacy-safe operational metrics

Done when 10–20 invited testers can use the product for two weeks without a critical privacy, delivery, or data-consistency defect.

## Milestone 5 — Play release

Deliverables:

- adaptive icon and launch assets
- signed Android App Bundle
- Play data-safety declaration
- screenshots and store listing
- internal and closed-track approval
- staged production rollout and rollback procedure

Done when a monitored staged rollout is live and the team can diagnose crashes and delivery failures without reading private invitation content.

