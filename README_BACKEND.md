# Backend EMS Store

API REST construida con Spring Boot, Java 21, Maven y MySQL Connector/J. El código y el Dockerfile están en `backend/`.

## Ejecutar localmente

Requisitos: Java 21 y una base MySQL/TiDB accesible. Desde PowerShell, inicia el backend en `backend/` con `mvnw.cmd spring-boot:run`. Antes, configura en el entorno local las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`. Para recuperación de contraseña por correo, configura también `MAIL_USERNAME` y `MAIL_PASSWORD`. No guardes sus valores en el repositorio.

La configuración local predeterminada apunta a MySQL en `localhost:3306/ems_store`. El backend escucha en el puerto indicado por `PORT` o, localmente, en 8080. Estado: `GET /api/health`.

## Desplegar en Render con TiDB Cloud

El archivo raíz `render.yaml` configura Render para construir `backend/Dockerfile` con Docker y Java 21. Crea el servicio desde un Blueprint conectado al repositorio. Si lo configuras manualmente, selecciona Docker y `backend` como directorio raíz.

Configura estos valores en Render; coloca los secretos únicamente en sus campos de variables de entorno:

- `DB_URL`: URL JDBC entregada por TiDB Cloud, con TLS habilitado. No incluyas credenciales en la URL.
- `DB_USERNAME` y `DB_PASSWORD`: credenciales del usuario de base de datos.
- `JWT_SECRET`: clave aleatoria de al menos 32 caracteres.
- `MAIL_USERNAME` y `MAIL_PASSWORD`: cuenta remitente y contraseña de aplicación del proveedor de correo.
- `APP_FRONTEND_URL`: origen del frontend publicado; permite CORS y construye enlaces de recuperación.

En TiDB Cloud, permite el acceso de red desde el servicio de Render y usa los datos del diálogo **Connect**. TiDB Cloud Starter y Essential requieren TLS. Después del despliegue, comprueba `https://<servicio>.onrender.com/api/health`.

## Endpoints principales

- Autenticación y cuenta: `/api/auth/**`.
- Productos, variantes, catálogos, inventario y movimientos: `/api/**`.
- Estado: `GET /api/health`.

La API requiere autenticación para las rutas protegidas. Configura las cuentas iniciales y sus contraseñas de forma segura antes de dar acceso al sistema.
