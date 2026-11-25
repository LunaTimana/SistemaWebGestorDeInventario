# Arranque local

Requisitos: Java 21, Node.js/npm y una instancia MySQL o TiDB accesible.

## Backend

1. Configura en el entorno local de Spring Boot las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`.
2. Para recuperación de contraseña por correo, configura además `MAIL_USERNAME` y `MAIL_PASSWORD` con una contraseña de aplicación.
3. Desde PowerShell, entra en `backend/` y ejecuta `mvnw.cmd spring-boot:run`.

La API queda disponible en `http://localhost:8080` por defecto. Puedes revisar `GET /api/health`.

## Frontend

1. Abre otra terminal en `ems-store-frontend/`.
2. Instala las dependencias con `npm install` y ejecuta `npm start`.
3. Abre `http://localhost:4200`.

Angular usa la API local por defecto. No guardes contraseñas, claves de firma ni credenciales de correo en archivos del proyecto. Para desplegar, sigue [README_BACKEND.md](README_BACKEND.md) y [la guía de Vercel](ems-store-frontend/README.es.md).
