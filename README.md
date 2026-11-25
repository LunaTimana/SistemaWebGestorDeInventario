# EMS Store

Sistema web para administrar productos, usuarios y movimientos de inventario.

## Estructura

- `ems-store-frontend/`: aplicación Angular.
- `backend/`: API REST Spring Boot con Java 21 y Maven.
- `DB_EMS_Prueba1.sql`: esquema y datos iniciales para una instalación local compatible.
- `render.yaml`: configuración del backend en Render.

## Despliegue

- Backend: servicio Docker en Render. Pasos y variables necesarias en [README_BACKEND.md](README_BACKEND.md).
- Frontend: sitio estático en Vercel. Pasos en [ems-store-frontend/README.es.md](ems-store-frontend/README.es.md).

Las credenciales de base de datos, correo y firma de tokens deben configurarse como variables de entorno en el proveedor o en el entorno local. No las guardes en este repositorio. Antes de exponer una base de datos, revisa y cambia las cuentas iniciales según corresponda.

## Desarrollo local

Se requieren Java 21, Node.js/npm y una base de datos MySQL/TiDB accesible. Configura las variables requeridas por Spring Boot en tu entorno local, inicia la API desde `backend/` con `mvnw.cmd spring-boot:run` y el frontend desde `ems-store-frontend/` con `npm start`.

La API local usa el puerto 8080 y Angular el 4200 por defecto. El endpoint de estado es `/api/health`.
