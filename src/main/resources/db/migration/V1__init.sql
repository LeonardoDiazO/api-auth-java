CREATE TABLE applications (
  app_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  app_name VARCHAR(200),
  description TEXT
);

CREATE TABLE users (
  user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  phone VARCHAR(20),
  password_hash TEXT NOT NULL,
  full_name VARCHAR(255),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_password_change TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  failed_login_attempts INT DEFAULT 0
);

CREATE TABLE roles (
  role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  role_name VARCHAR(200),
  app_id BIGINT
);

CREATE TABLE permissions (
  permission_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  permission_name VARCHAR(200),
  app_id BIGINT
);

CREATE TABLE rolepermissions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  role_id BIGINT,
  permission_id BIGINT
);

CREATE TABLE user_roles (
  user_role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  role_id BIGINT,
  app_id BIGINT
);

CREATE TABLE login_logs (
  log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  app_id BIGINT,
  login_time TIMESTAMP,
  ip_address VARCHAR(200),
  user_agent TEXT
);

CREATE TABLE passwordresets (
  reset_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  reset_token VARCHAR(255),
  expires_at TIMESTAMP,
  is_used BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_app_settings (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT,
  app_id BIGINT,
  setting_key VARCHAR(200),
  setting_value TEXT
);
