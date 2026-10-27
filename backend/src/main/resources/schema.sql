-- EMS Store: esquema mínimo idempotente para desarrollo.
-- Se ejecuta en cada arranque y solo crea las tablas que todavía no existen.
-- Las tablas de productos/variantes se mantienen compatibles con las entidades JPA.
CREATE DATABASE IF NOT EXISTS ems_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuarios (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(80) NOT NULL,
  apellido VARCHAR(80) NOT NULL,
  correo VARCHAR(150) NOT NULL UNIQUE,
  contrasena_hash VARCHAR(255) NOT NULL,
  debe_cambiar_contrasena BOOLEAN NOT NULL DEFAULT TRUE,
  rol_id INT NOT NULL,
  estado ENUM('activo','inactivo') NOT NULL DEFAULT 'activo',
  ultima_conexion DATETIME NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS marcas (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categorias (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS proveedores (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  contacto VARCHAR(120) NULL,
  telefono VARCHAR(30) NULL,
  correo VARCHAR(150) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS productos (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre_modelo VARCHAR(100) NOT NULL,
  marca_id INT NOT NULL,
  categoria_id INT NOT NULL,
  proveedor_id INT NULL,
  diseno VARCHAR(100) NULL,
  descripcion TEXT NULL,
  imagen_url VARCHAR(255) NULL,
  costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0,
  precio_venta DECIMAL(10,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_producto_marca FOREIGN KEY (marca_id) REFERENCES marcas(id),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
  CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS variantes_producto (
  id INT PRIMARY KEY AUTO_INCREMENT,
  producto_id INT NOT NULL,
  talla VARCHAR(20) NOT NULL,
  color VARCHAR(40) NOT NULL,
  codigo_unico VARCHAR(50) NOT NULL UNIQUE,
  stock_actual INT NOT NULL DEFAULT 0,
  stock_minimo INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_variante_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
  CONSTRAINT uq_variante UNIQUE (producto_id,talla,color),
  CONSTRAINT chk_stock_actual CHECK (stock_actual >= 0),
  CONSTRAINT chk_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS movimientos_inventario (
  id INT PRIMARY KEY AUTO_INCREMENT,
  variante_id INT NOT NULL,
  tipo ENUM('entrada','salida') NOT NULL,
  motivo ENUM('compra','venta','devolucion_proveedor','merma','ajuste') NOT NULL,
  cantidad INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_id INT NOT NULL,
  proveedor_id INT NULL,
  documento VARCHAR(50) NULL,
  destino_origen VARCHAR(100) NULL,
  comentario VARCHAR(255) NULL,
  CONSTRAINT fk_movimiento_variante FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
  CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_movimiento_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
  CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_movimiento_proveedor CHECK (tipo = 'entrada' OR proveedor_id IS NULL),
  CONSTRAINT chk_movimiento_motivo CHECK (
    (tipo='entrada' AND motivo IN ('compra','ajuste')) OR
    (tipo='salida' AND motivo IN ('venta','devolucion_proveedor','merma','ajuste'))
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Roles obligatorios para autorización por rol.
INSERT IGNORE INTO roles(id,nombre) VALUES (1,'Administrador'),(2,'Almacén'),(3,'Ventas');

-- Catálogos mínimos para que el formulario de producto no abra con combos vacíos.
INSERT INTO marcas(nombre) SELECT 'Nike' WHERE NOT EXISTS (SELECT 1 FROM marcas);
INSERT INTO categorias(nombre) SELECT 'Calzado Deportivo' WHERE NOT EXISTS (SELECT 1 FROM categorias);
INSERT INTO proveedores(nombre) SELECT 'Proveedor general' WHERE NOT EXISTS (SELECT 1 FROM proveedores);

-- Corrige el marcador de hash usado en versiones anteriores del SQL de demostración.
UPDATE usuarios SET contrasena_hash = '$2a$10$oKP0/RaV5uYgmCC4NZA1vOi8gu7URvRl9yUulMye/7vsQmPv.IuO2', debe_cambiar_contrasena = TRUE
WHERE contrasena_hash LIKE '%REEMPLAZAR_POR_HASH_BCRYPT_REAL%';

-- Usuario administrador inicial: contraseña temporal documentada como admin123.
-- Cambiarla desde Mi cuenta inmediatamente después del primer ingreso.
INSERT INTO usuarios(nombre,apellido,correo,contrasena_hash,debe_cambiar_contrasena,rol_id,estado)
SELECT 'Administrador','EMS Store','admin@emsstore.local', '$2a$10$oKP0/RaV5uYgmCC4NZA1vOi8gu7URvRl9yUulMye/7vsQmPv.IuO2', TRUE, 1, 'activo'
WHERE NOT EXISTS (SELECT 1 FROM usuarios);
