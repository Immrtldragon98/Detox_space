# Detox Space production-release runbook

This runbook is the required gate before sending Detox Space beyond invited testers.

## 1. Protect the signing identity

Create the Android upload keystore once, store an encrypted offline backup, and never commit it.

```bash
keytool -genkeypair -v -keystore detox-space-upload.jks -alias detox-space-upload -keyalg RSA -keysize 4096 -validity 10000
base64 -w 0 detox-space-upload.jks
```

In GitHub repository secrets, add:

- `DETOX_KEYSTORE_BASE64`
- `DETOX_KEYSTORE_PASSWORD`
- `DETOX_KEY_ALIAS`
- `DETOX_KEY_PASSWORD`

Then run **Publish Signed Android Test Release** from the Actions tab with the tag `v0.6.1-test.1`. It will fail before building if any signing secret is missing, run unit tests and Android lint, then publish a signed APK, signed AAB, and SHA-256 checksum in a GitHub prerelease.

Install the APK to test directly. Upload the AAB to Google Play Console instead. The first permanent signed APK cannot update an older debug-signed APK, so uninstall the old debug app once before installing it. Every later signed build from the same keystore will install as an update.

## 2. Verify the live backend

The public readiness probe must return HTTP 200 with `database: connected`:

```text
https://detox-space-api.onrender.com/health/ready
```

In Render, configure these production values:

- `DATABASE_URL`
- `JWT_SECRET` (at least 32 random characters)
- `ACCESS_TOKEN_TTL=15m`
- `NODE_ENV=production`
- `FIREBASE_PROJECT_ID`
- `FIREBASE_CLIENT_EMAIL`
- `FIREBASE_PRIVATE_KEY`

Keep the database provider's backups enabled. Set a Render health alert on `/health/ready` and an external uptime check against the same URL. Never put Firebase service-account JSON or the upload keystore in Git.

## 3. Complete the closed beta

Invite 10–20 testers for two weeks. Every release must pass the two-phone test in [`BETA_TESTING.md`](BETA_TESTING.md): connection code, bidirectional invitation, closed-app push, offline retry, session revocation, block/remove, and deletion.

Treat any privacy leak, duplicate invitation, missing notification, irrecoverable sign-in failure, or account-deletion failure as a launch blocker.

## 4. Google Play Console gate

Before rollout:

1. Enrol in Play App Signing and upload the signed AAB to the Internal testing track.
2. Set the app's privacy-policy URL to the public repository policy page or a hosted equivalent: `https://github.com/Immrtldragon98/Detox_space/blob/main/docs/PRIVACY_POLICY.md`.
3. Complete Data safety using the data list in [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md).
4. Add store description, email support address, app screenshots, feature graphic, and content rating.
5. Provide the in-app deletion path: `Settings → Delete account`.
6. Verify Play pre-launch report results on representative devices.

## 5. Staged release and rollback

Release first to internal testers, then closed testing. For public availability, start with the smallest staged rollout Play allows. Monitor crash reports, backend readiness, auth failures, and notification delivery daily. If a critical issue appears, halt the rollout in Play Console; the backend remains backward-compatible with the released client.

## Definition of production-ready

Detox Space is production-ready only when the signed AAB is accepted by Play, the two-week closed beta completes without launch blockers, live readiness monitoring is active, and the Play listing/privacy declarations are approved.
