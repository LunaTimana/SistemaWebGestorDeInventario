# Orden
__Base del proyecto:__ crear la app Angular, instalar Bootstrap y definir la estructura de carpetas por módulo.
__Layout principal:__ barra lateral y barra superior, con el menú filtrado por rol (su Tabla 7).
__Inicio de sesión:__ formulario con Reactive Forms y validaciones.
__Inventario: listado con filtros por modelo, talla y color, más detalle, registro y edición de producto.
__Movimientos:__ entradas, salidas e historial.
__Alertas de stock y dashboard.__
__Reportes y gestión de usuarios (solo Administrador).__

# PROTOTIPO DE ALTA FIDELIDAD DISEÑADO EN FIGMA 
https://www.figma.com/design/1VaQvUeBQMBf6rd2AjR8K9/EMS-STORE?node-id=0-1&t=yPnMECpYjREqq0zc-1 

## PRIMER AVANCE - LOGIN 
### QUÉ SE INSTALÓ

Comandos ejecutados en la terminal (en orden):<br>

  npm install -g @angular/cli<br>
  ng new ems-store-frontend --style=scss --routing<br>
  cd ems-store-frontend<br>
  npm install bootstrap @ng-bootstrap/ng-bootstrap chart.js ng2-charts<br>
  ng serve<br>
  npm install bootstrap-icons<br>
  ng generate component auth/login<br>

### Detalle de lo instalado:

- Angular CLI (instalación global): herramienta de línea de comandos para crear y gestionar el proyecto.<br>
- Proyecto ems-store-frontend: base de la aplicación Angular, creada con estilos SCSS y enrutamiento.<br>
- bootstrap: estilos y sistema de rejilla para una interfaz responsiva. Se activó con la línea @use 'bootstrap/scss/bootstrap'; en src/styles.scss.<br>
- @ng-bootstrap/ng-bootstrap: componentes de Bootstrap para Angular (modales, paginación, listas desplegables). Aún sin usar.<br>
- chart.js y ng2-charts: librerías de gráficos para el dashboard y los reportes. Aún sin usar.<br>
- bootstrap-icons: íconos de la interfaz.<br>

### QUÉ SE MODIFICÓ Y QUÉ SE AGREGÓ

Archivos modificados:<br>

- angular.json: se registró el CSS de bootstrap-icons en la sección "styles".<br>
- src/styles.scss: se agregaron las variables de color tomadas del prototipo de Figma (verde lima, fondos oscuros y grises), el estilo base del body y la clase reutilizable .tarjeta.<br>
- src/index.html: se agregó la carga de la fuente Inter.<br>
- src/app/app.html: ahora contiene únicamente <router-outlet />.<br>
- src/app/app.routes.ts: se agregó la ruta /login y la redirección desde la ruta raíz.<br>

Archivos agregados:<br>

- public/ems-logo.png: logo de EMS Store exportado desde Figma. <br>
- src/app/auth/login/login.ts: clase del componente de login (generada por el CLI, aún sin lógica). <br>
- src/app/auth/login/login.html: estructura del formulario de inicio de sesión  <br>(usuario, contraseña con botón MOSTRAR, botón de ingreso y enlace de recuperación). <br>
- src/app/auth/login/login.scss: estilos del login según el prototipo (fondo oscuro, tarjeta, campos, botón verde lima). <br>
- src/app/auth/login/login.spec.ts: archivo de pruebas generado por el CLI (no se utiliza). <br>

### ESTRUCTURA ACTUAL DEL PROYECTO

ems-store-frontend/
|-- angular.json                (modificado)
|-- package.json                (dependencias nuevas)
|-- public/
|   `-- ems-logo.png            (nuevo)
`-- src/
    |-- index.html              (modificado: fuente Inter)
    |-- main.ts
    |-- styles.scss             (modificado: variables, body, .tarjeta)
    `-- app/
        |-- app.ts              (sin cambios)
        |-- app.scss            (sin cambios)
        |-- app.spec.ts         (sin cambios)
        |-- app.config.ts       (sin cambios)
        |-- app.html            (modificado: router-outlet)
        |-- app.routes.ts       (modificado: ruta login)
        `-- auth/
            `-- login/
                |-- login.ts        (generado, sin lógica todavía)
                |-- login.html      (nuevo)
                |-- login.scss      (nuevo)
                `-- login.spec.ts   (generado)