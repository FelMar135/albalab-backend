--liquibase formatted sql

--changeset albalab:usuarios-1
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
--rollback DROP TABLE usuarios;

--changeset albalab:usuarios-2
INSERT INTO usuarios (email, password, role, fecha_creacion)
SELECT 'felipe@gmai.com', '$2y$10$7EDU674nGN3m7A.wxf93j.FH/HJTwdlkb3xL8N.b9/gRvGPeC5rOG', 'USER', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'felipe@gmai.com');

INSERT INTO usuarios (email, password, role, fecha_creacion)
SELECT 'matias@gmail.com', '$2y$10$GqXV5W7sTmNAokq0FOU4ke/3pK66HYnkiiEG5Xsi5urnEkDf/YRWy', 'USER', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'matias@gmail.com');
--rollback DELETE FROM usuarios WHERE email IN ('felipe@gmai.com', 'matias@gmail.com');
