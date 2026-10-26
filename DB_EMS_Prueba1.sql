-- ============================================================
-- EMS STORE - Sistema Web de Gestión de Inventario
-- Esquema de base de datos (MySQL 8.0.16 o superior)
--
-- Versión ajustada a las pantallas del prototipo:
--   * Una sola tienda (no hay tabla de sucursales).
--   * El precio y el costo van por producto (no por variante).
--   * Incluye datos de ejemplo al final del script.
--
-- Requiere MySQL 8.0.16+ para que los CHECK se apliquen de verdad.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ems_store
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ems_store;

-- ============================================================
-- 1. USUARIOS Y ROLES
-- ============================================================

-- Roles del sistema (Administrador, Almacén, Ventas)
CREATE TABLE roles (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre          VARCHAR(50) NOT NULL UNIQUE
);

-- Usuarios del sistema
CREATE TABLE usuarios (
    id                       INT PRIMARY KEY AUTO_INCREMENT,
    nombre                   VARCHAR(80)  NOT NULL,
    apellido                 VARCHAR(80)  NOT NULL,
    correo                   VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash          VARCHAR(255) NOT NULL,           -- RNF01: hash BCrypt, nunca la contraseña
    debe_cambiar_contrasena  BOOLEAN      NOT NULL DEFAULT TRUE, -- TRUE al crear con contraseña temporal
    rol_id                   INT          NOT NULL,
    estado                   ENUM('activo', 'inactivo') NOT NULL DEFAULT 'activo',
    ultima_conexion          DATETIME     NULL,
    fecha_creacion           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- ============================================================
-- 2. CATÁLOGOS (listas fijas, no texto libre)
-- ============================================================

CREATE TABLE marcas (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre          VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE categorias (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre          VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE proveedores (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre          VARCHAR(120) NOT NULL,
    contacto        VARCHAR(120),
    telefono        VARCHAR(30),
    correo          VARCHAR(150)
);

-- ============================================================
-- 3. PRODUCTOS Y VARIANTES
-- ============================================================

-- Producto "base" (agrupa variantes por modelo). El costo y el precio
-- se guardan aquí: son los mismos para todas las tallas y colores.
CREATE TABLE productos (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    nombre_modelo   VARCHAR(100)  NOT NULL,
    marca_id        INT           NOT NULL,
    categoria_id    INT           NOT NULL,
    proveedor_id    INT           NULL,       -- "Proveedor asociado" al registrar el producto
    diseno          VARCHAR(100),
    descripcion     TEXT,
    imagen_url      VARCHAR(255),
    costo_unitario  DECIMAL(10,2) NOT NULL,   -- en soles
    precio_venta    DECIMAL(10,2) NOT NULL,   -- en soles
    CONSTRAINT fk_producto_marca     FOREIGN KEY (marca_id)     REFERENCES marcas(id),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT chk_producto_costo    CHECK (costo_unitario >= 0),
    CONSTRAINT chk_producto_precio   CHECK (precio_venta >= 0)
);

-- Variante específica del producto: combinación modelo-talla-color (RF01)
CREATE TABLE variantes_producto (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    producto_id     INT         NOT NULL,
    talla           VARCHAR(20) NOT NULL,
    color           VARCHAR(40) NOT NULL,
    codigo_unico    VARCHAR(50) NOT NULL UNIQUE,   -- SKU generado por la aplicación
    stock_actual    INT         NOT NULL DEFAULT 0,
    stock_minimo    INT         NOT NULL DEFAULT 0, -- usado en la pantalla de Alertas
    CONSTRAINT fk_variante_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    -- RF01: la combinación modelo-talla-color no puede repetirse
    CONSTRAINT uq_variante UNIQUE (producto_id, talla, color),
    -- RF03: segunda barrera para que el stock nunca quede negativo
    CONSTRAINT chk_stock_actual CHECK (stock_actual >= 0),
    CONSTRAINT chk_stock_minimo CHECK (stock_minimo >= 0)
);

-- ============================================================
-- 4. MOVIMIENTOS DE INVENTARIO (RF02, RF03)
-- ============================================================
-- La cantidad siempre es positiva; la dirección la da "tipo".
-- Los ajustes y las pérdidas se registran con motivo 'ajuste' o 'merma':
--   * 'ajuste' puede ser entrada (se encontró stock) o salida (faltante).
--   * 'merma'  es una salida por pérdida o daño.
CREATE TABLE movimientos_inventario (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    variante_id     INT NOT NULL,
    tipo            ENUM('entrada', 'salida') NOT NULL,
    motivo          ENUM('compra', 'venta', 'devolucion_proveedor', 'merma', 'ajuste') NOT NULL,
    cantidad        INT NOT NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id      INT NOT NULL,            -- responsable que registró el movimiento
    proveedor_id    INT NULL,                -- solo aplica si tipo = 'entrada'
    documento       VARCHAR(50)  NULL,       -- Boleta #4325, Guía #9021, Físico Audit...
    destino_origen  VARCHAR(100) NULL,       -- ej. "Caja Principal" en una salida
    comentario      VARCHAR(255) NULL,
    CONSTRAINT fk_movimiento_variante  FOREIGN KEY (variante_id)  REFERENCES variantes_producto(id),
    CONSTRAINT fk_movimiento_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuarios(id),
    CONSTRAINT fk_movimiento_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_movimiento_proveedor CHECK (tipo = 'entrada' OR proveedor_id IS NULL),
    CONSTRAINT chk_movimiento_motivo CHECK (
        (tipo = 'entrada' AND motivo IN ('compra', 'ajuste'))
        OR
        (tipo = 'salida'  AND motivo IN ('venta', 'devolucion_proveedor', 'merma', 'ajuste'))
    )
);

-- ============================================================
-- 5. ÍNDICES
-- (las llaves foráneas y los UNIQUE ya crean los suyos)
-- ============================================================
CREATE INDEX idx_movimientos_fecha ON movimientos_inventario (fecha);  -- filtro por rango de fechas
CREATE INDEX idx_productos_nombre  ON productos (nombre_modelo);       -- búsqueda por nombre

-- ============================================================
-- 6. VISTAS (reglas de negocio derivadas, sin duplicar datos)
-- ============================================================

-- Pantalla de Inventario: estado de cada variante
--   Sin Stock  -> stock = 0
--   Bajo Stock -> stock por debajo del mínimo
--   Disponible -> resto
CREATE OR REPLACE VIEW vista_inventario AS
SELECT
    v.id            AS variante_id,
    v.codigo_unico  AS sku,
    m.nombre        AS marca,
    p.nombre_modelo AS producto,
    c.nombre        AS categoria,
    v.talla,
    v.color,
    v.stock_actual  AS stock,
    v.stock_minimo,
    p.precio_venta  AS precio,
    CASE
        WHEN v.stock_actual = 0                THEN 'Sin Stock'
        WHEN v.stock_actual < v.stock_minimo   THEN 'Bajo Stock'
        ELSE 'Disponible'
    END AS estado
FROM variantes_producto v
JOIN productos  p ON p.id = v.producto_id
JOIN marcas     m ON m.id = p.marca_id
JOIN categorias c ON c.id = p.categoria_id;

-- Pantalla de Alertas: variantes por debajo del mínimo.
-- REGLA PROPUESTA (el equipo debe aprobarla y anotarla en el informe):
--   CRITICO -> stock menor o igual al 50 % del mínimo
--   RIESGO  -> stock por debajo del mínimo pero sobre ese 50 %
CREATE OR REPLACE VIEW vista_alertas_stock AS
SELECT
    v.id            AS variante_id,
    v.codigo_unico  AS sku,
    p.nombre_modelo AS producto,
    v.stock_actual,
    v.stock_minimo,
    v.stock_actual - v.stock_minimo AS deficit,
    CASE
        WHEN v.stock_actual <= v.stock_minimo * 0.5 THEN 'CRITICO'
        ELSE 'RIESGO'
    END AS estado,
    (SELECT MAX(mi.fecha)
       FROM movimientos_inventario mi
      WHERE mi.variante_id = v.id
        AND mi.tipo = 'entrada'
        AND mi.motivo = 'compra') AS ultimo_ingreso
FROM variantes_producto v
JOIN productos p ON p.id = v.producto_id
WHERE v.stock_actual < v.stock_minimo;

-- ============================================================
-- 7. DATOS DE EJEMPLO (solo para desarrollo y pruebas)
-- ============================================================

INSERT INTO roles (id, nombre) VALUES
    (1, 'Administrador'),
    (2, 'Almacén'),
    (3, 'Ventas');

-- SOLO DEMO LOCAL: los usuarios activos de ejemplo comparten la clave admin123.
-- Reemplaza el hash antes de desplegar o exponer esta base de datos.
SET @hash_demo = '$2a$10$7dPScLhBuKjGVwO1hztAxuxu0BJ9grwnfeMQM02OcUEtD3P/V5HQy';

INSERT INTO usuarios (id, nombre, apellido, correo, contrasena_hash, debe_cambiar_contrasena, rol_id, estado, ultima_conexion) VALUES
    (1, 'Marcio', 'Morales', 'admin@emsstore.com', @hash_demo, FALSE, 1, 'activo',   '2026-10-22 15:45:00'),
    (2, 'Carlos', 'Mendoza', 'cmendoza@emsstore.com', @hash_demo, FALSE, 2, 'activo',   '2026-10-22 08:30:00'),
    (3, 'Luz',    'Benitez', 'lbenitez@emsstore.com', @hash_demo, FALSE, 3, 'activo',   '2026-10-21 18:22:00'),
    (4, 'Juan',   'Sanchez', 'jsanchez@emsstore.com', @hash_demo, FALSE, 2, 'inactivo', '2026-10-17 10:00:00');

INSERT INTO marcas (id, nombre) VALUES
    (1, 'Nike'),
    (2, 'Adidas'),
    (3, 'Under Armour'),
    (4, 'Puma');

INSERT INTO categorias (id, nombre) VALUES
    (1, 'Calzado Deportivo'),
    (2, 'Ropa Deportiva'),
    (3, 'Accesorios');

INSERT INTO proveedores (id, nombre) VALUES
    (1, 'Nike Distribuciones S.A.'),
    (2, 'UA Distribuidora'),
    (3, 'Distribuidora Adidas'),
    (4, 'Distribuidora Puma');

-- Los costos son valores de ejemplo; ajústalos a los reales de la tienda.
INSERT INTO productos (id, nombre_modelo, marca_id, categoria_id, proveedor_id, costo_unitario, precio_venta) VALUES
    (1, 'Zapatillas Pegasus 40 Zoom',        1, 1, 1,  72.00, 120.00),
    (2, 'Polera de Entrenamiento TechFit',   2, 2, 3,  14.50,  29.99),
    (3, 'Cortavientos Storm Impermeable',    3, 2, 2,  45.00,  85.00),
    (4, 'Shorts de Running Dri-FIT Flex',    1, 2, 1,  19.00,  39.99),
    (5, 'Zapatillas Deviate Nitro Elite',    4, 1, 4, 105.00, 180.00),
    (6, 'Camiseta Puma Cool Running',        4, 2, 4,  17.00,  35.00);

-- El stock_actual se calcula más abajo a partir de los movimientos.
INSERT INTO variantes_producto (id, producto_id, talla, color, codigo_unico, stock_actual, stock_minimo) VALUES
    (1, 1, 'US 10', 'Azul/Rojo',   'NK-PEG40-10', 0, 15),
    (2, 2, 'M',     'Negro',       'AD-TFIT-M',   0, 15),
    (3, 3, 'L',     'Gris',        'UA-STORM-L',  0, 10),
    (4, 4, 'S',     'Azul Marino', 'NK-DRIT-S',   0, 10),
    (5, 5, '43',    'Neon Verde',  'PM-DEVI-43',  0,  8),
    (6, 6, 'S',     'Blanco',      'PM-COOL-S',   0,  8);

INSERT INTO movimientos_inventario
    (variante_id, tipo, motivo, cantidad, fecha, usuario_id, proveedor_id, documento, destino_origen, comentario) VALUES
    -- Zapatillas Pegasus 40 Zoom (queda en 15)
    (1, 'entrada', 'compra',  3, '2026-10-02 09:10:00', 2, 1,    'Guía #8800',    NULL,             'Ingreso inicial'),
    (1, 'salida',  'venta',   2, '2026-10-08 12:20:00', 3, NULL, 'Boleta #4101',  'Caja Principal', NULL),
    (1, 'salida',  'venta',   1, '2026-10-12 17:05:00', 3, NULL, 'Boleta #4180',  'Caja Principal', NULL),
    (1, 'entrada', 'compra', 15, '2026-10-20 10:30:00', 2, 1,    'Guía #9021',    NULL,             NULL),
    -- Polera TechFit (queda en 8)
    (2, 'entrada', 'compra', 22, '2026-10-18 09:40:00', 2, 3,    'Guía #8911',    NULL,             NULL),
    (2, 'salida',  'venta',   7, '2026-10-19 11:15:00', 3, NULL, 'Boleta #4321',  'Caja Principal', NULL),
    (2, 'salida',  'venta',   7, '2026-10-20 16:45:00', 3, NULL, 'Boleta #4322',  'Caja Principal', NULL),
    -- Cortavientos Storm (queda en 0)
    (3, 'entrada', 'compra', 18, '2026-10-15 10:00:00', 2, 2,    'Guía #8950',    NULL,             NULL),
    (3, 'salida',  'venta',   8, '2026-10-16 13:10:00', 3, NULL, 'Boleta #4210',  'Caja Principal', NULL),
    (3, 'salida',  'venta',   9, '2026-10-18 18:00:00', 3, NULL, 'Boleta #4265',  'Caja Principal', NULL),
    (3, 'salida',  'ajuste',  1, '2026-10-21 17:45:00', 1, NULL, 'Físico Audit',  NULL,             'Diferencia detectada en el conteo físico'),
    -- Shorts Dri-FIT Flex (queda en 24)
    (4, 'entrada', 'compra', 30, '2026-10-02 09:30:00', 2, 1,    'Guía #8801',    NULL,             NULL),
    (4, 'salida',  'venta',   4, '2026-10-10 15:20:00', 3, NULL, 'Boleta #4150',  'Caja Principal', NULL),
    (4, 'salida',  'venta',   2, '2026-10-21 14:32:00', 1, NULL, 'Boleta #4325',  'Caja Principal', NULL),
    -- Zapatillas Deviate Nitro Elite (queda en 12)
    (5, 'entrada', 'compra', 20, '2026-10-04 11:00:00', 2, 4,    'Guía #8830',    NULL,             NULL),
    (5, 'salida',  'venta',   8, '2026-10-18 12:30:00', 3, NULL, 'Boleta #4270',  'Caja Principal', NULL),
    -- Camiseta Puma Cool Running (queda en 1)
    (6, 'entrada', 'compra', 10, '2026-10-05 10:15:00', 2, 4,    'Guía #8840',    NULL,             NULL),
    (6, 'salida',  'venta',   9, '2026-10-19 16:00:00', 3, NULL, 'Boleta #4300',  'Caja Principal', NULL);

-- Calcula el stock actual de cada variante a partir de sus movimientos.
-- (El WHERE evita el error 1175 del "Safe Updates" de MySQL Workbench.)
UPDATE variantes_producto v
SET v.stock_actual = (
    SELECT COALESCE(SUM(CASE WHEN m.tipo = 'entrada' THEN m.cantidad ELSE -m.cantidad END), 0)
    FROM movimientos_inventario m
    WHERE m.variante_id = v.id
)
WHERE v.id > 0;

-- ============================================================
-- NOTAS PARA EL BACKEND
-- ============================================================
-- Control de acceso por rol (se implementa en Spring Security, no en SQL):
--   Administrador -> acceso completo (productos, movimientos, reportes, usuarios).
--   Almacén       -> movimientos de inventario y consulta de catálogo.
--   Ventas        -> consulta de catálogo/stock y registro de salidas por venta.
--
-- Registrar un movimiento: guardar el movimiento y actualizar
--   variantes_producto.stock_actual en la MISMA transacción (@Transactional).
--   Antes, validar en el servicio que una salida no supere el stock actual;
--   el CHECK de la tabla es solo la segunda barrera.
--
-- SKU: los códigos de ejemplo (NK-PEG40-10) no incluyen el color. La
--   restricción uq_variante evita duplicar modelo-talla-color, pero si dos
--   colores de la misma talla generan el mismo código, chocarán con el UNIQUE
--   de codigo_unico. Define la regla de generación (incluir el color o usar
--   una secuencia).
--
-- Pantalla de Movimientos:
--   * "Tipo Operación" se muestra así: motivo = 'ajuste' -> 'Ajuste';
--     si no, tipo = 'entrada' -> 'Ingreso' y tipo = 'salida' -> 'Salida'.
--   * La tarjeta "Ajustes y pérdidas" cuenta los movimientos con
--     motivo IN ('ajuste', 'merma').
--   * El ID que se muestra (MV-9284) es 'MV-' + id.
--
-- Reportes (RF05): se generan con consultas agregadas sobre
--   movimientos_inventario y variantes_producto. Margen comercial:
--   (precio_venta - costo_unitario) / precio_venta.
-- ============================================================
