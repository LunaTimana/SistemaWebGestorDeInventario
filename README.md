# SistemaWebGestorDeInventario

## Ejecución local en Windows

Requisitos: MySQL 8.0.16 o superior, Java 21 y Node.js/npm. Mantén MySQL en ejecución mientras uses el sistema.

1. En MySQL Workbench, abre y ejecuta `DB_EMS_Prueba1.sql`. El script crea la base `ems_store`, sus tablas y datos de ejemplo. Si ya tienes el esquema creado, no vuelvas a ejecutar el script completo.
2. Abre una primera ventana de PowerShell en la carpeta del proyecto y ejecuta:

   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "tucontraseña"
   $jwtBytes = [byte[]]::new(32)
   [Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
   $env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
   Set-Location .\backend
   .\mvnw.cmd spring-boot:run
   ```

   Deja esta ventana abierta. El backend debe indicar que arrancó en el puerto **8083**. La conexión predeterminada es `localhost:3306/ems_store`; `DB_URL` permite cambiarla. Hibernate valida el esquema existente y no crea ni modifica tablas.
3. Abre una segunda ventana de PowerShell en la carpeta del proyecto y ejecuta:

   ```powershell
   Set-Location .\ems-store-frontend
   npm start
   ```

   Abre la dirección que muestre Angular (normalmente `http://localhost:4200`). El proxy de desarrollo reenvía `/api` a `http://localhost:8083`; abre el frontend mediante Angular, no como archivo local ni con otro servidor.
4. Inicia sesión con `admin@emsstore.com` y `admin123` (cuenta local de demostración).

Para confirmar el backend desde otra ventana de PowerShell, prueba el login directamente:

```powershell
$body = @{ correo = "admin@emsstore.com"; contrasena = "admin123" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8083/api/auth/login -ContentType "application/json" -Body $body
```

Si esta petición falla, revisa primero la ventana del backend, las credenciales de MySQL y que `DB_EMS_Prueba1.sql` se haya ejecutado. Si responde con un token, pero la pantalla no entra, revisa que Angular se haya iniciado con `npm start` en el puerto 4200 y recarga la página.

Las variables definidas con `$env:` solo viven en esa ventana de PowerShell. Si abres una nueva, vuelve a definirlas antes de iniciar el backend. `JWT_SECRET` debe ser una clave Base64 de al menos 32 bytes; el bloque de arriba genera una al azar.

Para ejecutar las pruebas del backend (usan H2 en memoria y no requieren MySQL):

```powershell
Set-Location .\backend
.\mvnw.cmd test
```

Apache POI (`poi-ooxml`) está configurado en Maven para leer archivos `.xlsx`. La API de importación es `POST http://localhost:8083/api/importaciones/stock` con `multipart/form-data`: campo `archivo` requerido, `confirmar` opcional (por defecto `false`) y `responsableId` requerido al confirmar la importación.

## Seguridad (RNF01)

- El inicio de sesión es `POST /api/auth/login`, JSON `{ "correo": "...", "contrasena": "..." }`. Devuelve un JWT Bearer válido por 1 hora y los datos del usuario. Las demás rutas `/api/**` requieren `Authorization: Bearer <token>`.
- `GET /api/usuarios/**` es exclusivo de Administrador. Registrar productos e importar stock requiere Administrador o Almacén. Las cuentas inactivas no pueden iniciar sesión. Las contraseñas de la tabla `usuarios` se verifican con BCrypt. El SQL incluye cuentas de demo activas con contraseña `admin123`; cambia sus claves antes de desplegar o exponer la base.
- La cuenta de administrador de demostración inicia con `admin@emsstore.com` y contraseña `admin123`. Si ya habías creado la base de datos con una versión anterior del script, actualiza el correo en MySQL: `UPDATE usuarios SET correo = 'admin@emsstore.com' WHERE correo = 'mmorales@emsstore.com';`.
- Genera una clave JWT distinta por ambiente y no la subas al repositorio:

  ```powershell
  $jwtBytes = [byte[]]::new(32)
  [Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
  $env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
  ```

- En producción inicia con el perfil `prod` y define `TLS_KEY_STORE`, `TLS_KEY_STORE_PASSWORD`, `TLS_KEY_ALIAS` y `CORS_ALLOWED_ORIGIN`. El almacén debe ser PKCS12 y contener un certificado válido para el dominio. El backend escuchará por HTTPS en el puerto 8443. Configura Angular detrás del mismo dominio con `/api` dirigido al backend; en desarrollo `npm start` usa el proxy local HTTP.
- Angular guarda el token en `sessionStorage`, añade el encabezado Bearer y lleva al login si no hay una sesión válida. Cierra la pestaña para eliminar la sesión.
Desarrollo de un sistema administrativo web que permita catalogar productos, controlar en tiempo real las entradas y salidas de inventario, generar alertas de stock mínimo, anticipar la demanda y emitir reportes gerenciales de ventas y rotación.  

## Resumen de tecnologías y librerías: EMS Store
### Frontend
__Categoría__ :	Tecnología / librería <br>
__Framework__ :	Angular (TypeScript) <br>
__Estilos y UI__ :	Bootstrap 5, SCSS, ng-bootstrap (o Angular Material) <br>
__Formularios__ :	Reactive Forms (incluido en Angular) <br>
__Gráficos__ :	ng2-charts (Chart.js) o ngx-charts <br>
__Comunicación con la API__ :	HttpClient + HttpInterceptor (para adjuntar el JWT) <br>
__Control de acceso__ :	Route Guards por rol (Administrador, Almacén, Ventas) <br>
__Rutas__ :	Angular Router con carga diferida (lazy loading) <br>
__Pruebas__ :	Pruebas manuales de la interfaz en el navegador (login, registro de entradas y salidas, filtros, alertas y control por rol), documentadas en la matriz de casos de prueba <br>
### Backend
__Categoría__ :	Tecnología / librería <br>
__Lenguaje y framework__ :	Java 17+ y Spring Boot 3 <br>
__API__ :	Spring Web (REST) <br>
__Persistencia__ :	Spring Data JPA (Hibernate) <br>
__Base de datos__ :	MySQL 8 + driver mysql-connector-j <br>
__Seguridad__ :	Spring Security, JWT (jjwt), BCrypt <br>
__Validación__ :	Bean Validation (@Valid) <br>
__Mapeo DTO__ :	MapStruct (opcional) y Lombok (opcional) // Clases DTO simples escritas manualmente (sin librerías adicionales) <br>
__Documentación de la API__ :	springdoc-openapi (Swagger UI) <br>
__Reportes Excel__ :	Apache POI <br>
__Reportes PDF__ :	JasperReports (o OpenPDF) <br>
__Tareas programadas y correo__ :	@Scheduled y Spring Mail (para alertas por correo, opcional) <br>
__Migraciones de BD__ :	Script SQL del esquema generado desde MySQL Workbench, versionado en GitHub <br>
__Pruebas__ :	Pruebas manuales de endpoints con Postman y Swagger UI, documentadas en una matriz de casos de prueba <br>
__Construcción__ :	Maven o Gradle <br>

## Qué cubre cada tecnología en los requerimientos
__Requerimiento__ -> Cómo se resuelve <br>
__RF01 Productos y variantes con código único__ -> JPA + restricción UNIQUE + Reactive Forms <br>
__RF02 / RF03 Entradas y salidas con stock automático__ -> Servicio con @Transactional y validación de stock <br>
__RF04 Consulta con filtros__ -> Consultas JPA con Pageable + filtros en Angular <br>
__RF05 Reportes__ -> Chart.js para visualización, Apache POI/Jasper para exportar <br>
__Alertas de stock mínimo__ -> Consulta stock_actual <= stock_minimo + aviso en dashboard <br>
__RNF01 Seguridad__ -> Spring Security + JWT + BCrypt + HTTPS <br>
__RNF02 Menos de 2 s__ -> Índices y paginación en el servidor <br>
__RNF03 Responsivo y fácil de usar__ -> Bootstrap 5 + diseño de Figma <br>
__Migración desde Excel__ -> Apache POI como importador con validación de duplicados <br>
