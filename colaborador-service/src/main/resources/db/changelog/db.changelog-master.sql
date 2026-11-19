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

--changeset albalab:colaboradores-4
INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'felipe@gmai.com', 'Felipe', NULL, 5000, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY), 'APROBADA', 'DON-SEED-FELIPE-5000-1'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-FELIPE-5000-1');

INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'felipe@gmai.com', 'Felipe', NULL, 5000, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY), 'APROBADA', 'DON-SEED-FELIPE-5000-2'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-FELIPE-5000-2');

INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'felipe@gmai.com', 'Felipe', NULL, 5000, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 3 DAY), 'APROBADA', 'DON-SEED-FELIPE-5000-3'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-FELIPE-5000-3');

INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'felipe@gmai.com', 'Felipe', NULL, 2500, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 2 DAY), 'APROBADA', 'DON-SEED-FELIPE-2500-1'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-FELIPE-2500-1');

INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'felipe@gmai.com', 'Felipe', NULL, 2500, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 1 DAY), 'APROBADA', 'DON-SEED-FELIPE-2500-2'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-FELIPE-2500-2');

INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'matias@gmail.com', 'Matias', NULL, 10000, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 6 DAY), 'APROBADA', 'DON-SEED-MATIAS-10000-1'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-MATIAS-10000-1');

INSERT INTO colaboradores (usuario_email, nombre, foto_uri, monto_donacion, es_anonimo, fecha, estado, webpay_orden)
SELECT 'matias@gmail.com', 'Matias', NULL, 4000, FALSE, DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 2 DAY), 'APROBADA', 'DON-SEED-MATIAS-4000-1'
WHERE NOT EXISTS (SELECT 1 FROM colaboradores WHERE webpay_orden = 'DON-SEED-MATIAS-4000-1');
--rollback DELETE FROM colaboradores WHERE webpay_orden LIKE 'DON-SEED-%';
