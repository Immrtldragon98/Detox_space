# Detox Space — Product Specification

Status: V0 approved direction  
Platform: Native Android  
Product principle: **The app succeeds when people leave the app and spend time together.**

## 1. Product definition

Detox Space is a private coordination app for people who already know and trust each other. It helps someone answer two small questions without opening a feed or starting a long chat:

1. Who is open to spending time together?
2. What small thing can we do now or soon?

It is the native Android successor to Dot Space. It keeps the trusted-connection, presence, privacy, device-session, realtime, and tiny-signal ideas while changing the product language from online presence to real shared moments.

## 2. Problem

People may want company but continue scrolling because initiating a plan feels like work. Normal messaging requires composing a message, negotiating availability, and handling social pressure. Public friendship products introduce profiles, strangers, feeds, and matching.

Detox Space reduces the initiation cost to one private, low-pressure invitation.

## 3. Target user

V0 is for adults who already have trusted relationships:

- close friends
- siblings and family
- partners
- roommates
- small existing friend circles

Finding or matching with strangers is outside V0.

## 4. Core loop

1. A user opens Detox Space.
2. They choose `Available`, `Quiet`, or `Away`.
3. They see trusted connections and the presence each person chose to share.
4. They select one person.
5. They send a tiny invitation with a time window.
6. The recipient selects `I'm in`, `Later`, or `Not today`.
7. Accepted invitations become a moment with a clear time.
8. The app sends at most one useful reminder and then becomes quiet.
9. The invitation expires automatically.

## 5. V0 vocabulary

### Presence

| State | Meaning |
|---|---|
| Available | Open to a small invitation |
| Quiet | Around, but does not want invitations now |
| Away | Not available |

Presence is manually controlled, optional, and expires. It must never be inferred from location, phone use, or background activity.

### Invitations

Initial presets:

| Invitation | Intent |
|---|---|
| Walk? | Step outside together |
| Coffee? | Take a small offline break |
| Talk? | Spend time talking or listening |
| Free? | Let the other person suggest the moment |

An invitation contains:

- sender
- recipient
- preset type
- optional short note
- `Now`, `Later today`, or a selected time
- creation and expiration time
- current delivery/response state

### Responses

| Response | Meaning |
|---|---|
| I'm in | Accept the invitation |
| Later | Interested, but not at the proposed time |
| Not today | Decline without explanation |

There are no read receipts in V0. A user should not feel watched or pressured.

## 6. Invitation state machine

```text
DRAFT -> SENDING -> SENT -> DELIVERED
                         |-> ACCEPTED -> COMPLETED
                         |-> LATER
                         |-> DECLINED
                         |-> EXPIRED
                         |-> CANCELLED
```

Rules:

- Only trusted, accepted connections can exchange invitations.
- A recipient can disable invitations from any connection.
- Invitations expire automatically.
- Repeated unanswered invitations are rate-limited.
- A sender may cancel an unanswered invitation.
- Declining never requires a reason.
- Completed moments are private; there are no public posts or scores.

## 7. V0 screens

### Onboarding

- Product promise
- Sign in or create account
- Notification explanation and permission
- Privacy defaults

### People

- Current presence control
- Trusted connections
- Connection availability
- Send-invitation action

### Invitation composer

- Person selected
- Invitation preset
- Time window
- Optional note
- Expiration preview
- Send confirmation

### Inbox

- Incoming invitations requiring a response
- Accepted/upcoming moments
- Recently resolved invitations

### Connections

- Add using private invite code or link
- Pending requests
- Per-connection invitation permission
- Remove or block

### Settings and devices

- Notification preferences
- Presence expiry default
- Active signed-in devices
- Revoke a device
- Export/delete account data
- Privacy policy and support

## 8. Explicit non-goals

V0 will not include:

- strangers or public discovery
- dating or matching
- profiles optimized around appearance
- feeds, posts, stories, likes, comments, followers, or streaks
- screen-time monitoring or app blocking
- background location or live location
- contact-book upload
- automatic availability inference
- public events or large groups
- AI recommendations
- advertising or paid boosts
- public popularity, attendance, or safety scores

## 9. Trust and privacy requirements

- Private by default.
- Collect the minimum data needed for the feature.
- Never request location permission in V0.
- Never expose phone contacts automatically.
- Store Android authentication material in encrypted platform storage.
- Use short-lived access tokens and revocable device sessions.
- Enforce authorization and invitation permissions on the server, not only in the UI.
- Rate-limit connection requests and invitations.
- Support remove, block, device revocation, and account deletion.
- Do not put private invitation text in lock-screen notifications by default.
- Log security events without logging private invitation content.

## 10. Notification philosophy

Allowed notifications:

- trusted connection request
- incoming invitation
- accepted invitation
- one imminent-moment reminder
- security/device warning

Not allowed:

- engagement reminders
- streak warnings
- “people miss you” messages
- popularity summaries
- prompts designed only to reopen the app

## 11. Success metrics

The primary product metric is not time in app.

V0 measures:

- invitations sent
- percentage receiving a response
- percentage accepted
- accepted invitations marked completed
- median time from sending to response
- notification delivery reliability
- weekly users who complete at least one moment
- blocks, reports, and invitation-mute rate

Do not rank people or show these metrics publicly.

## 12. V0 definition of done

V0 is ready for a closed test when two users on different networks can:

1. Create accounts.
2. Connect through a private invitation.
3. Control and share presence.
4. Send a time-bound invitation.
5. Receive it while the app is open, backgrounded, or closed.
6. Accept, defer, or decline it.
7. See consistent state after reconnecting or changing devices.
8. Disable signals, remove/block the connection, and revoke another device.
9. Use the core flow without location or contact permissions.
10. Complete automated unit, API, database, and Android UI tests.

