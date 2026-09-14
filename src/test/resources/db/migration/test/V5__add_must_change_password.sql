-- Espejo H2 de la V6 principal (esta carpeta de test nunca tuvo la V5 de
-- main, que solo borraba una tabla que aquí nunca existió -- ver
-- src/main/resources/db/migration/V5__drop_user_app_settings.sql).
alter table users add column must_change_password boolean not null default false;
