-- ================================================================
-- V3: Associate users with apps, add lockout and email verification
-- Designed for fresh installation (empty tables).
-- ================================================================

-- Add new columns to users
ALTER TABLE users ADD COLUMN app_id BIGINT;
ALTER TABLE users ADD COLUMN locked_until DATETIME NULL;
ALTER TABLE users ADD COLUMN is_email_verified BOOLEAN DEFAULT FALSE;

-- Remove global unique constraints (username/email are now unique per app)
ALTER TABLE users DROP INDEX username;
ALTER TABLE users DROP INDEX email;

-- Add per-app unique constraints
ALTER TABLE users ADD CONSTRAINT uk_username_app UNIQUE (username, app_id);
ALTER TABLE users ADD CONSTRAINT uk_email_app UNIQUE (email, app_id);

-- Make app_id non-nullable
ALTER TABLE users MODIFY COLUMN app_id BIGINT NOT NULL;

-- Allow nullable password_hash (social-only users have no password)
ALTER TABLE users MODIFY COLUMN password_hash TEXT NULL;

-- Foreign key: user belongs to an application
ALTER TABLE users
  ADD CONSTRAINT users_app_id_fkey
  FOREIGN KEY (app_id) REFERENCES applications(app_id) ON DELETE RESTRICT;

-- Add FK to passwordresets (was missing in V1)
ALTER TABLE passwordresets
  ADD CONSTRAINT passwordresets_user_id_fkey
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
