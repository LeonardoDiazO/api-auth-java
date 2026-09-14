-- Fuerza el cambio de contraseña en el próximo login: se activa al crear un
-- usuario con contraseña puesta por un admin, o al restablecer una
-- contraseña. Default false para no afectar a los usuarios ya existentes
-- (auto-registrados, ya conocen su propia contraseña).
alter table users add column must_change_password boolean not null default false;
