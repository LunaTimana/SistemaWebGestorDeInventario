
-- ============================================================
-- EMS STORE - Sistema Web de Gestión de Inventario
-- Esquema de base de datos (MySQL)
-- ============================================================

-- Tabla de roles del sistema (Administrador, Almacén, Ventas)
CREATE TABLE roles (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre          VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla de usuarios del sistema
CREATE TABLE usuarios (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    nombre              VARCHAR(100) NOT NULL,
    correo              VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash     VARCHAR(255) NOT NULL,  -- RNF01: hash, no cifrado reversible
    rol_id              INT NOT NULL,
    estado              ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
    fecha_creacion      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Producto "base" (agrupa variantes por modelo)
CREATE TABLE productos (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre_modelo   VARCHAR(100) NOT NULL,
    categoria       VARCHAR(80),
    diseno          VARCHAR(100),
    descripcion     TEXT,
    imagen_url      VARCHAR(255)
);

-- Variante específica del producto: combinación modelo-talla-color (RF01)
CREATE TABLE variantes_producto (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    producto_id     INT NOT NULL,
    talla           VARCHAR(20) NOT NULL,
    color           VARCHAR(40) NOT NULL,
    codigo_unico    VARCHAR(50) NOT NULL UNIQUE,  -- generado: modelo-talla-color
    stock_actual    INT NOT NULL DEFAULT 0,
    stock_minimo    INT NOT NULL DEFAULT 0,       -- usado en pantalla de Alertas
    CONSTRAINT fk_variante_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- Tabla de proveedores (tabla fija, no campo de texto libre)
CREATE TABLE proveedores (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre          VARCHAR(120) NOT NULL,
    contacto        VARCHAR(120),
    telefono        VARCHAR(30),
    correo          VARCHAR(150)
);

-- Movimientos de inventario: entradas y salidas (RF02, RF03)
CREATE TABLE movimientos_inventario (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    variante_id     INT NOT NULL,
    tipo            ENUM('entrada', 'salida') NOT NULL,
    cantidad        INT NOT NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id      INT NOT NULL,           -- quién registró el movimiento
    proveedor_id    INT NULL,               -- FK a proveedores; solo aplica si tipo = 'entrada'
    motivo          VARCHAR(50),            -- venta / ajuste; solo aplica si tipo = 'salida'
    comentario      VARCHAR(255),           -- opcional
    CONSTRAINT fk_movimiento_variante FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_movimiento_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
);

-- ============================================================
-- Control de acceso por rol (a implementar en la capa de aplicación,
-- no como restricción de la base de datos):
--
--   Administrador  -> acceso completo: agregar/editar producto,
--                     generar reportes, registrar movimientos
--                     (entradas/salidas), gestión de usuarios.
--   Almacén        -> acceso a movimientos de inventario
--                     (entradas/salidas) y consulta de catálogo.
--   Ventas         -> acceso a consulta de catálogo/stock y
--                     registro de salidas (por venta).
--
-- La tabla "roles" ya permite identificar el rol del usuario
-- autenticado (usuarios.rol_id); la validación de qué endpoint
-- o pantalla puede usar cada rol se hace en el backend, no en SQL.
-- ============================================================

-- Notas adicionales:
-- - Reportes (RF05: rotación, ventas, stock) se generan con
--   consultas agregadas sobre movimientos_inventario y
--   variantes_producto; no requieren tabla propia.
-- - Alertas de stock mínimo se calculan en tiempo real con:
--   SELECT * FROM variantes_producto WHERE stock_actual < stock_minimo;
-- ============================================================