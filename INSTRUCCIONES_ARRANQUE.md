# EMS Store — instrucciones de arranque

Esta guía explica cómo iniciar EMS Store localmente en Windows. Se necesitan MySQL 8, Java 21, Node.js y npm. La primera ejecución necesita conexión a Internet para descargar dependencias.

## 1. Preparar la base de datos

1. Inicia el servicio de MySQL.
2. Abre `DB_EMS_Prueba1.sql` con MySQL Workbench y ejecútalo para crear `ems_store`, sus tablas y datos de demostración.
3. Si la base de datos ya existe, no vuelvas a ejecutar el script completo. Para una base existente a la que le falte la columna de género de las variantes, ejecuta una sola vez `backend/src/main/resources/db/migration/V20261120_01__agregar_genero_variante.sql`.

El usuario de demostración es `admin@emsstore.com` y su contraseña es `admin123`. Úsalo solo en desarrollo local.

## 2. Configurar e iniciar el backend

Abre una ventana de PowerShell en la carpeta raíz del proyecto (`SistemaWebGestorDeInventario`) y ejecuta:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/ems_store?createDatabaseIfNotExist=true"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CLAVE_DE_MYSQL"

$jwtBytes = [byte[]]::new(32)
[Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)

Set-Location .\backend
.\mvnw.cmd spring-boot:run
```

Si MySQL no tiene contraseña, puedes establecer `$env:DB_PASSWORD = ""`. Mantén esta ventana abierta. El backend estará disponible en `http://localhost:8080`.

### Configurar Gmail para recuperar contraseñas

Este paso es necesario para enviar enlaces de recuperación. En la cuenta Gmail remitente, activa la verificación en dos pasos y crea una contraseña de aplicación. No uses la contraseña normal de Gmail.

Antes de iniciar el backend, define estas variables en la misma ventana de PowerShell:

```powershell
$env:MAIL_USERNAME = "tu-cuenta@gmail.com"
$env:MAIL_PASSWORD = "CONTRASENA_DE_APLICACION_GMAIL"
$env:APP_FRONTEND_URL = "http://localhost:4200"
```

La contraseña de aplicación no debe guardarse en el repositorio. El enlace enviado vence a los 30 minutos y solo puede utilizarse una vez.

## 3. Iniciar el frontend

Abre otra ventana de PowerShell en la carpeta raíz del proyecto y ejecuta:

```powershell
Set-Location .\ems-store-frontend
npm install
npm start
```

Cuando Angular termine de iniciar, abre `http://localhost:4200`. El frontend se conecta al backend en `http://localhost:8080`.

## 4. Iniciar sesión y recuperar una contraseña

- Inicia sesión localmente con `admin@emsstore.com` y `admin123`.
- Para recuperar una contraseña, pulsa **¿Olvidaste tu contraseña?**, escribe el correo de la cuenta y sigue el enlace recibido por correo.
- Si falta la configuración de Gmail, el formulario mostrará el error del backend. Comprueba `MAIL_USERNAME`, `MAIL_PASSWORD` y que la contraseña sea de aplicación.

## Comprobación rápida del backend

Desde una tercera ventana de PowerShell, puedes comprobar el inicio de sesión con:

```powershell
$body = @{ correo = "admin@emsstore.com"; contrasena = "admin123" } | ConvertTo-Json
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body $body
```

Una respuesta con un token indica que la API pudo autenticar la cuenta de demostración.

## Problemas frecuentes

- **Access denied for user**: revisa `DB_USERNAME` y `DB_PASSWORD`.
- **Unknown database `ems_store`**: inicia MySQL y ejecuta `DB_EMS_Prueba1.sql`.
- **El puerto 8080 ya está ocupado**: cierra el proceso que lo utiliza o inicia el backend con `$env:SERVER_PORT = "8081"`. Si cambias el puerto, actualiza también la URL de API del frontend.
- **No llega el correo**: confirma que Gmail tiene verificación en dos pasos, que la contraseña de aplicación es correcta y que las variables SMTP se definieron en la ventana donde corre Spring Boot.
- **Las variables parecen no aplicarse**: las variables `$env:` solo existen en la ventana actual de PowerShell; vuelve a definirlas en la ventana donde inicias el backend.

## Detener la aplicación

En cada ventana donde corre un servidor, pulsa `Ctrl+C`. Puedes cerrar MySQL cuando ya no necesites la aplicación.
