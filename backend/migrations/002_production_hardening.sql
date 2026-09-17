CREATE UNIQUE INDEX IF NOT EXISTS device_sessions_refresh_token_unique
  ON device_sessions(refresh_token_hash);

CREATE INDEX IF NOT EXISTS connection_invites_expiry_idx
  ON connection_invites(expires_at)
  WHERE accepted_at IS NULL;

CREATE INDEX IF NOT EXISTS invitations_expiry_idx
  ON invitations(expires_at)
  WHERE state IN ('SENT', 'LATER');
