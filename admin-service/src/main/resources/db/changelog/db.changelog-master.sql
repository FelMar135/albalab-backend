--liquibase formatted sql

--changeset albalab:1
CREATE TABLE IF NOT EXISTS administradores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT uk_administradores_email UNIQUE (email)
);
--rollback DROP TABLE administradores;

--changeset albalab:2
INSERT INTO administradores (email, password, role, fecha_creacion)
SELECT 'admin@albalab.cl',
       '$2a$10$L5cux66/45niJMxXpvwVkuxXIriE6RNHu4vovSlOE.ihQN1BjnQKy',
       'ADMIN',
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM administradores WHERE email = 'admin@albalab.cl'
);
--rollback DELETE FROM administradores WHERE email = 'admin@albalab.cl';
