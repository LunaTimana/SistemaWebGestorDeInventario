# EMS Store - Backend integrado

Backend construido dentro de la estructura original del proyecto, conservando `backend/`, `ems-store-frontend/` y `DB_EMS_Prueba1.sql`.

## Conexion MySQL (siguiendo el patron de ProyWeb)

La conexion se configura en `backend/src/main/resources/application-mysql.properties` mediante variables de entorno:
- `DB_URL` (por defecto `jdbc:mysql://localhost:3306/ems_store`)
- `DB_USERNAME` (por defecto `root`)
- `DB_PASSWORD` (por defecto vacia)
- `SPRING_PROFILES_ACTIVE` (por defecto `mysql`)

Antes de iniciar, ejecuta el archivo `DB_EMS_Prueba1.sql` en MySQL Workbench. No cambies los nombres de tablas: este backend usa el esquema original plural (`productos`, `marcas`, `categorias`, `proveedores`, `variantes_producto`).

## Ejecutar en Windows

Abre PowerShell en la carpeta `backend` y ejecuta:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/ems_store"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
.\mvnw.cmd spring-boot:run
```

Si tu usuario root no tiene contraseña, deja `DB_PASSWORD` vacia:
`$env:DB_PASSWORD=""`

Requiere Java 21 y acceso a internet la primera vez para descargar dependencias Maven.

### Recuperar contraseña con Gmail

El flujo de recuperación envía un enlace de un solo uso que vence en 30 minutos. En la cuenta Gmail que enviará los mensajes, activa la verificación en dos pasos y crea una contraseña de aplicación. No uses la contraseña normal de Gmail ni guardes la contraseña de aplicación en el repositorio.

En PowerShell, antes de iniciar el backend, configura:

```powershell
$env:MAIL_USERNAME="tu-cuenta@gmail.com"
$env:MAIL_PASSWORD="CONTRASENA_DE_APLICACION_SIN_ESPACIOS"
$env:APP_FRONTEND_URL="http://localhost:4200"
```

La contraseña de aplicación se crea desde la seguridad de la cuenta Google; Google puede ocultar esa opción si la cuenta no tiene verificación en dos pasos o está administrada por una organización. Pégala sin espacios; el backend también elimina espacios al leerla. La tabla `password_reset_tokens` se crea al iniciar el backend mediante `schema.sql`.

## Endpoints implementados

- `GET /api/productos` y `GET /api/productos?q=laptop`
- `GET /api/productos/{id}`
- `POST /api/productos`, `PUT /api/productos/{id}`, `DELETE /api/productos/{id}`
- CRUD de `/api/marcas`, `/api/categorias`, `/api/proveedores`
- `GET /api/variantes`, `GET /api/variantes?productoId=1`
- `GET /api/variantes/alertas-stock`
- `GET /api/variantes/{id}`, `POST /api/variantes`, `PUT /api/variantes/{id}`, `DELETE /api/variantes/{id}`

Swagger UI: http://localhost:8080/swagger-ui/index.html

## Ejemplo POST producto

```json
{
  "nombreModelo": "Laptop de prueba",
  "marcaId": 1,
  "categoriaId": 1,
  "proveedorId": null,
  "diseno": "Negro",
  "descripcion": "Producto de prueba",
  "imagenUrl": null,
  "costoUnitario": 1800.00,
  "precioVenta": 2200.00
}
```

Los IDs de marca y categoría deben existir previamente. La eliminacion puede fallar si hay variantes asociadas, lo cual protege las relaciones de la base de datos.

## Importante

- `ddl-auto=validate`: el backend valida el esquema existente y no crea ni modifica tablas automaticamente.
- Los endpoints actuales no incluyen autenticacion. La configuracion se deja abierta para desarrollo local; antes de publicar el sistema debe agregarse autenticacion y autorizacion.
- Los movimientos de inventario y login no se implementan en esta entrega; el backend cubre CRUD de productos/catalogos/variantes y consulta de alertas de stock.

## Si no inicia

- `Access denied for user`: confirma `DB_USERNAME` y `DB_PASSWORD`.
- `Unknown database ems_store`: ejecuta primero `DB_EMS_Prueba1.sql`.
- `Schema-validation`: comprueba que la base corresponde a ese SQL original, no a otro esquema con tablas en singular.
- `Port 8080 already in use`: cierra el otro backend o cambia `SERVER_PORT`.


## Seguridad, movimientos y Excel

El login ahora devuelve un token Bearer firmado; las llamadas privadas deben incluirlo. Define `JWT_SECRET` en el entorno y no uses el secreto de desarrollo en producción. La API `POST /api/movimientos` actualiza stock e historial en una sola transacción; `GET /api/movimientos` consulta el historial. `POST /api/inventario/importar/excel` recibe multipart/form-data con el campo `archivo` y procesa las columnas SKU, Stock actual y Stock mínimo de un `.xlsx` exportado desde el sistema. La importación solo modifica SKU ya existentes y registra ajustes cuando cambia el stock.
