-- Serialize before identity defaults are evaluated, not in a row trigger.
-- Readers take this same transaction lock so uncommitted lower IDs cannot be skipped.
CREATE FUNCTION lock_invitation_changes() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  PERFORM pg_advisory_xact_lock(724831902);
  RETURN NULL;
END;
$$;
CREATE TRIGGER invitations_change_lock
BEFORE INSERT OR UPDATE ON invitations
FOR EACH STATEMENT EXECUTE FUNCTION lock_invitation_changes();

CREATE FUNCTION advance_invitation_cursor() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  NEW.sequence_id := nextval(pg_get_serial_sequence('invitations', 'sequence_id'));
  NEW.updated_at := now();
  RETURN NEW;
END;
$$;
CREATE TRIGGER invitations_change_cursor
BEFORE UPDATE ON invitations
FOR EACH ROW EXECUTE FUNCTION advance_invitation_cursor();
