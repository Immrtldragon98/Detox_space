# Detox Space Privacy Notice (Beta)

Last updated: 17 September 2026

Detox Space is a private Android app for trusted people to exchange small, time-bound invitations.

## Data we process

- Account username, email address, and password hash
- Trusted connections and single-use connection codes
- Device-session information needed for sign-in and remote revocation
- Invitation type, optional note, proposed time, response, and expiry
- Firebase Cloud Messaging token used for private notifications

## Data we do not collect

Detox Space does not request precise location, contacts, photos, microphone, camera, advertising identifiers, or background activity data. It has no public profile, feed, follower count, likes, or stranger discovery.

## Notifications

Push notifications are deliberately generic and do not contain a person's identity, invitation type, or note. The app securely synchronizes details after opening.

## Retention and deletion

Users can revoke device sessions, remove or block connections, and permanently delete their account from Settings. Account deletion removes the account and associated sessions, connections, invitations, and blocks from the active database.

## Security

Passwords are hashed with Argon2. Access tokens are short-lived, refresh tokens rotate, sessions can be revoked, and API requests are rate-limited. No internet service can guarantee absolute security.

## Contact

For privacy or beta-support requests, contact `vyvsyadav98@proton.me`.
