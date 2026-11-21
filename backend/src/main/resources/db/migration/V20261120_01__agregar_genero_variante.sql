-- Ejecutar una sola vez en la base ems_store existente antes de iniciar el backend.
ALTER TABLE variantes_producto
    ADD COLUMN genero VARCHAR(20) NOT NULL DEFAULT 'Hombre' AFTER talla;

ALTER TABLE variantes_producto
    DROP INDEX uq_variante,
    ADD CONSTRAINT uq_variante UNIQUE (producto_id, talla, color, genero);
