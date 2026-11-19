--liquibase formatted sql

--changeset albalab:colaboradores-1
CREATE TABLE IF NOT EXISTS colaboradores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    foto_uri VARCHAR(500) DEFAULT NULL,
    monto_donacion INT DEFAULT NULL
);
--rollback DROP TABLE colaboradores;

--changeset albalab:colaboradores-2
ALTER TABLE colaboradores
    ADD COLUMN usuario_id BIGINT NULL,
    ADD COLUMN usuario_email VARCHAR(150) NULL,
    ADD COLUMN es_anonimo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN fecha DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'APROBADA',
    ADD COLUMN webpay_orden VARCHAR(100) NULL;
--rollback ALTER TABLE colaboradores DROP COLUMN usuario_id, DROP COLUMN usuario_email, DROP COLUMN es_anonimo, DROP COLUMN fecha, DROP COLUMN estado, DROP COLUMN webpay_orden;

--changeset albalab:colaboradores-3
CREATE INDEX idx_colaboradores_usuario ON colaboradores (usuario_id, fecha);
CREATE INDEX idx_colaboradores_usuario_email ON colaboradores (usuario_email, fecha);
CREATE UNIQUE INDEX uk_colaboradores_webpay_orden ON colaboradores (webpay_orden);
--rollback DROP INDEX idx_colaboradores_usuario ON colaboradores;
--rollback DROP INDEX idx_colaboradores_usuario_email ON colaboradores;
--rollback DROP INDEX uk_colaboradores_webpay_orden ON colaboradores;
