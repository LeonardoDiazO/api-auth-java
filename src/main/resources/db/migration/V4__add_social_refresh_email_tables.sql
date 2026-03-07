-- ================================================================
-- V4: Social accounts, refresh tokens, email verifications
-- ================================================================

-- Social accounts: links a user to a Google (or future) provider identity
CREATE TABLE social_accounts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  provider VARCHAR(50) NOT NULL,
  provider_user_id VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  -- A Google identity can only map to one user
  CONSTRAINT uk_provider_provideruserid UNIQUE (provider, provider_user_id)
);

-- Refresh tokens with sliding window (60 min inactivity = expiry)
CREATE TABLE refresh_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  app_id BIGINT NOT NULL,
  expires_at DATETIME NOT NULL,
  revoked BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_rt_app FOREIGN KEY (app_id) REFERENCES applications(app_id) ON DELETE CASCADE
);

-- Email verification tokens
CREATE TABLE email_verifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  is_used BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ev_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Indices
CREATE INDEX idx_refresh_tokens_user_app ON refresh_tokens(user_id, app_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_social_accounts_user_id ON social_accounts(user_id);
CREATE INDEX idx_email_verifications_token ON email_verifications(token);
