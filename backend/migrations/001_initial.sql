CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  username text NOT NULL,
  email text NOT NULL,
  password_hash text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  CONSTRAINT users_username_unique UNIQUE (username),
  CONSTRAINT users_email_unique UNIQUE (email),
  CONSTRAINT users_username_format CHECK (username ~ '^[a-zA-Z0-9_]{3,24}$')
);

CREATE TABLE IF NOT EXISTS device_sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  device_name text NOT NULL,
  refresh_token_hash text NOT NULL,
  fcm_token text,
  created_at timestamptz NOT NULL DEFAULT now(),
  last_seen_at timestamptz NOT NULL DEFAULT now(),
  revoked_at timestamptz
);
CREATE INDEX IF NOT EXISTS device_sessions_user_idx ON device_sessions(user_id);

CREATE TABLE IF NOT EXISTS connection_invites (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  code_hash text NOT NULL UNIQUE,
  created_by uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at timestamptz NOT NULL,
  accepted_by uuid REFERENCES users(id) ON DELETE SET NULL,
  accepted_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS connections (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_low uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_high uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  low_allows_invitations boolean NOT NULL DEFAULT true,
  high_allows_invitations boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT connection_order CHECK (user_low < user_high),
  CONSTRAINT connection_unique UNIQUE (user_low, user_high)
);
CREATE INDEX IF NOT EXISTS connections_low_idx ON connections(user_low);
CREATE INDEX IF NOT EXISTS connections_high_idx ON connections(user_high);

CREATE TABLE IF NOT EXISTS invitations (
  id uuid PRIMARY KEY,
  sender_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  recipient_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  connection_id uuid NOT NULL REFERENCES connections(id) ON DELETE CASCADE,
  signal_type text NOT NULL,
  note text,
  proposed_at timestamptz NOT NULL,
  expires_at timestamptz NOT NULL,
  state text NOT NULL DEFAULT 'SENT',
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  sequence_id bigint GENERATED ALWAYS AS IDENTITY,
  CONSTRAINT invitations_not_self CHECK (sender_id <> recipient_id),
  CONSTRAINT invitations_note_length CHECK (note IS NULL OR char_length(note) <= 80),
  CONSTRAINT invitations_signal_type CHECK (signal_type IN ('WALK', 'COFFEE', 'TALK', 'FREE')),
  CONSTRAINT invitations_state CHECK (state IN ('SENT','ACCEPTED','LATER','DECLINED','EXPIRED','CANCELLED','COMPLETED')),
  CONSTRAINT invitations_expiry CHECK (expires_at > created_at)
);
CREATE INDEX IF NOT EXISTS invitations_sender_idx ON invitations(sender_id, sequence_id DESC);
CREATE INDEX IF NOT EXISTS invitations_recipient_idx ON invitations(recipient_id, sequence_id DESC);

CREATE TABLE IF NOT EXISTS blocks (
  blocker_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  blocked_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (blocker_id, blocked_id),
  CONSTRAINT blocks_not_self CHECK (blocker_id <> blocked_id)
);
