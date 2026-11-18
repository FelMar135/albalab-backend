--liquibase formatted sql

--changeset albalab:1
CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    precio INT NOT NULL,
    descripcion VARCHAR(1000),
    imagen_url VARCHAR(500),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6) NOT NULL,
    CONSTRAINT uk_productos_nombre UNIQUE (nombre)
);
--rollback DROP TABLE productos;

--changeset albalab:2
INSERT INTO productos (nombre, precio, descripcion, imagen_url, disponible, fecha_creacion)
SELECT 'Difusor Aroma',
       12000,
       'Aromatizador de ambiente en spray. Envase pet. 250 ml. Formato ideal para el hogar. Aromas disponibles: Vainilla.',
       NULL,
       TRUE,
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM productos WHERE nombre = 'Difusor Aroma'
);
--rollback DELETE FROM productos WHERE nombre = 'Difusor Aroma';

--changeset albalab:3
INSERT INTO productos (nombre, precio, descripcion, imagen_url, disponible, fecha_creacion)
SELECT 'Bolsa AlbaLab',
       4000,
       'Bolsa ecologica de algodon medidas 40x60cm. Lavable y reutilizable. Personalizada con frase Alba Lab.',
       NULL,
       TRUE,
       CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (
    SELECT 1 FROM productos WHERE nombre = 'Bolsa AlbaLab'
);
--rollback DELETE FROM productos WHERE nombre = 'Bolsa AlbaLab';