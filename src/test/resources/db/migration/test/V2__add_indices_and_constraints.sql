-- V2: indices and constraints — H2 compatible
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_app_id ON user_roles(app_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

CREATE INDEX idx_users_email ON users(email);

CREATE INDEX idx_login_logs_user_id ON login_logs(user_id);
CREATE INDEX idx_login_logs_login_time ON login_logs(login_time);

CREATE INDEX idx_rolepermissions_role_id ON rolepermissions(role_id);
CREATE INDEX idx_rolepermissions_permission_id ON rolepermissions(permission_id);

ALTER TABLE user_roles ADD CONSTRAINT uk_user_role_app UNIQUE (user_id, role_id, app_id);
ALTER TABLE rolepermissions ADD CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id);

ALTER TABLE user_roles ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE user_roles ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE;
ALTER TABLE user_roles ADD CONSTRAINT user_roles_app_id_fkey FOREIGN KEY (app_id) REFERENCES applications(app_id) ON DELETE CASCADE;

ALTER TABLE rolepermissions ADD CONSTRAINT rolepermissions_role_id_fkey FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE;
ALTER TABLE rolepermissions ADD CONSTRAINT rolepermissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE;

ALTER TABLE login_logs ADD CONSTRAINT login_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
ALTER TABLE login_logs ADD CONSTRAINT login_logs_app_id_fkey FOREIGN KEY (app_id) REFERENCES applications(app_id) ON DELETE CASCADE;
