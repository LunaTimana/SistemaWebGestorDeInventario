# EMS Store - Backend integrado

./mvnw clean package -> empaqueta en JAR, construye la aplicaicon con maven

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
$env:MAIL_USERNAME="tu-cuenta@gmail.com"
$env:MAIL_PASSWORD="CONTRASENA_DE_APLICACION_SIN_ESPACIOS"
$env:JWT_SECRET="mi-clave-super-secreta-para-jwt-2026"
.\mvnw.cmd spring-boot:run
```

Si tu usuario root no tiene contraseña, deja `DB_PASSWORD` vacia:
`$env:DB_PASSWORD=""`

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

## Desplegar el backend en Render con TiDB Cloud

El backend está separado en `backend/` y se despliega como un servicio Docker con Java 21. El manifiesto `render.yaml` señala `backend/` como raíz del servicio; así Render no usa Node para compilar el backend. El frontend Angular permanece en `ems-store-frontend/` y se despliega como un servicio independiente.

1. Sube estos cambios al repositorio conectado a Render.
2. En Render, crea un **Blueprint** desde el repositorio y revisa el servicio `ems-store-backend` de `render.yaml`. Si lo creas manualmente, elige **Docker** como Runtime y `backend` como Root Directory.
3. En TiDB Cloud, abre **Connect** para tu clúster y copia el host, el usuario, la contraseña y el nombre de la base. Autoriza las conexiones entrantes necesarias en la lista de acceso del clúster.
4. Completa en Render las variables solicitadas:

   - `DB_URL`: `jdbc:mysql://<HOST_TIDB>:4000/<BASE_DE_DATOS>?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3`
   - `DB_USERNAME`: el usuario que muestra TiDB Cloud (en Starter/Essential puede incluir un prefijo del clúster).
   - `DB_PASSWORD`: contraseña de ese usuario.
   - `JWT_SECRET`: secreto aleatorio de al menos 32 caracteres.
   - `APP_FRONTEND_URL`: origen publicado del frontend Angular, por ejemplo `https://ems-store-frontend.onrender.com` (sin ruta final).
   - `MAIL_USERNAME`: dirección Gmail que enviará los correos de recuperación.
   - `MAIL_PASSWORD`: contraseña de aplicación de Google para esa cuenta, sin espacios; no uses la contraseña normal de Gmail.

   TiDB Cloud requiere TLS para los planes Starter y Essential. La URL de arriba fuerza TLS y valida el certificado y el nombre del host.
5. Despliega. Render revisará `GET /api/health`; cuando el servicio esté activo, esa ruta debe responder JSON y mostrar la base conectada.

No uses `localhost` en `DB_URL` en Render: allí `localhost` apunta al contenedor del backend, no a TiDB. Conserva la URL JDBC como `DB_URL` y coloca las credenciales en las variables separadas.

La URL del backend aparecerá en Render como `https://<servicio>.onrender.com`. Al publicar Angular por separado, su configuración de API debe apuntar a `https://<servicio>.onrender.com/api`; el origen de Angular debe coincidir con `APP_FRONTEND_URL` para que CORS permita las llamadas.

Para obtener `MAIL_PASSWORD`, habilita la verificación en dos pasos en la cuenta Google y crea una contraseña de aplicación desde la configuración de seguridad de la cuenta. Guárdala como variable secreta en Render.
