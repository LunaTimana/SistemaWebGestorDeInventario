# EMS Store — tecnologías y librerías

Este documento resume las tecnologías y dependencias declaradas en el proyecto y su función. Se basa en `backend/pom.xml`, `ems-store-frontend/package.json` y el código fuente. Algunas dependencias están instaladas, pero todavía no se importan en la aplicación; se indican por separado para no confundirlas con las que ya están en uso.

## Arquitectura

EMS Store es una aplicación web con frontend y backend separados. El frontend Angular se comunica mediante HTTP con una API REST creada con Spring Boot. La API guarda los datos en MySQL y ofrece funciones de inventario, usuarios, movimientos, reportes y recuperación de contraseñas por correo.

| Capa | Tecnología principal | Uso |
|---|---|---|
| Frontend | Angular y TypeScript | Interfaz web y lógica de las pantallas. |
| Backend | Java 21 y Spring Boot 4.1.2 | API REST, reglas de negocio, seguridad y acceso a datos. |
| Base de datos | MySQL 8 o superior | Usuarios, catálogo, productos, variantes y movimientos de inventario. |
| Comunicación | HTTP y JSON | Intercambio entre Angular y los endpoints `/api/**`. |

## Frontend

| Tecnología o librería | Versión declarada | Uso en EMS Store |
|---|---:|---|
| Angular (`core`, `common`, `platform-browser`) | `^22.2.0` | Componentes, renderizado de la aplicación y servicios HTTP. |
| TypeScript | `~6.0.2` | Lenguaje del frontend; implementa componentes, servicios, guards e interceptores. |
| Angular Router | `^22.2.0` | Navegación entre inicio de sesión, inventario, movimientos, reportes y demás pantallas. |
| Angular Forms | `^22.2.0` | Formularios de acceso, productos, movimientos y recuperación de contraseña. Se usa `FormsModule` con formularios enlazados a componentes. |
| Angular HttpClient (`@angular/common`) | `^22.2.0` | Solicitudes HTTP al backend y descarga de reportes. |
| RxJS | `~7.8.0` | Manejo de respuestas asíncronas de HttpClient mediante observables. |
| Bootstrap | `^5.3.8` | Estilos base, grilla y utilidades visuales; se carga desde `src/styles.scss`. |
| Bootstrap Icons | `^1.13.2` | Iconos de navegación, botones, alertas y otras pantallas. |
| SCSS | Integrado en Angular | Estilos encapsulados de componentes y estilos globales. |
| tslib | `^2.3.0` | Funciones auxiliares que usa el código generado de TypeScript. |

### Dependencias frontend declaradas que aún no se usan en el código

| Tecnología o librería | Versión declarada | Estado |
|---|---:|---|
| ng-bootstrap (`@ng-bootstrap/ng-bootstrap`) | `^21.0.0` | Está en `package.json`, pero no se encontraron imports en `src`; los componentes actuales usan Angular y estilos propios/Bootstrap. |
| Chart.js | `^4.5.1` | Está instalado, pero no se encontraron imports en `src`. |
| ng2-charts | `^11.0.0` | Está instalado, pero no se encontraron imports en `src`. |

### Herramientas de desarrollo frontend

| Herramienta | Versión declarada | Uso |
|---|---:|---|
| Angular CLI (`@angular/cli`) | `^22.2.3` | Comandos de desarrollo, compilación y ejecución (`npm start`, `npm run build`). |
| Angular Build (`@angular/build`) | `^22.2.3` | Construcción y servidor de desarrollo de Angular. |
| Angular Compiler CLI | `^22.2.0` | Compilación de plantillas y código Angular. |
| Prettier | `^3.8.1` | Formateo de código. |
| Vitest | `^5.0.0` | Infraestructura de pruebas declarada para el frontend. |
| jsdom | `^30.0.0` | Entorno DOM simulado para pruebas de interfaz. |

## Backend

| Tecnología o librería | Versión declarada | Uso en EMS Store |
|---|---:|---|
| Java | 21 | Lenguaje del backend. |
| Spring Boot | `4.1.2` | Plataforma que configura y ejecuta la aplicación backend. |
| Spring Web MVC (`spring-boot-starter-webmvc`) | Administrada por Spring Boot | Controladores REST, rutas `/api/**`, respuestas JSON y carga/descarga de archivos. |
| Spring Data JPA y Hibernate | Administradas por Spring Boot | Mapeo de entidades y persistencia para productos, variantes y catálogos; incluye repositorios JPA. |
| Spring JDBC (`JdbcTemplate`) | Administrado por Spring Boot | Consultas y actualizaciones SQL directas para autenticación, movimientos, alertas, usuarios y reportes. |
| Spring Security | Administrada por Spring Boot | Reglas de acceso, filtros de seguridad y codificación BCrypt de contraseñas. |
| Spring Security OAuth2 Resource Server | Administrada por Spring Boot | Dependencia incluida en el proyecto. La autenticación observada en el código utiliza `TokenService` y un filtro Bearer propio. |
| Spring Validation / Jakarta Bean Validation | Administrada por Spring Boot | Validación de cuerpos de solicitud y campos, por ejemplo correo, campos obligatorios y longitudes. |
| Spring Mail y JavaMailSender | Administradas por Spring Boot | Envío de enlaces de recuperación de contraseña por SMTP de Gmail con STARTTLS. |
| MySQL Connector/J | Administrado por Spring Boot | Driver JDBC para conectar Spring Boot con MySQL. |
| Apache POI (`poi-ooxml`) | `5.4.1` | Lectura de archivos Excel `.xlsx` para importar inventario y generación de reportes Excel. |
| OpenPDF | `2.0.4` | Generación de reportes PDF de inventario y movimientos. |
| Maven Wrapper | Incluido en `backend/` | Ejecuta Maven con `mvnw.cmd` sin requerir una instalación global de Maven. |

### Seguridad implementada

- Los tokens de acceso se generan y validan con código propio (`TokenService`) y viajan como Bearer token.
- Spring Security aplica permisos por ruta y rol; un filtro propio extrae el token de las solicitudes.
- BCrypt almacena contraseñas como hashes.
- La recuperación genera un token temporal, guarda su hash y envía el enlace por Gmail SMTP.
- `JWT_SECRET`, `MAIL_USERNAME` y `MAIL_PASSWORD` se configuran con variables de entorno; no deben guardarse credenciales reales en el repositorio.

### Dependencias de prueba del backend

El `pom.xml` declara módulos de prueba de Spring Boot para JPA, Security, Validation y Web MVC, todos con alcance `test`. No son dependencias necesarias para iniciar la aplicación en uso normal.

## Base de datos y configuración

- **MySQL 8+** almacena las tablas relacionales del sistema.
- `DB_EMS_Prueba1.sql` contiene el esquema y los datos de demostración para preparar una base local.
- `backend/src/main/resources/schema.sql` crea tablas auxiliares, incluida `password_reset_tokens`, al iniciar el backend.
- `application.properties` configura conexión JDBC, JPA, SMTP Gmail y URL base del frontend a través de variables de entorno.
- Spring Boot escucha por defecto en el puerto `8080`; Angular inicia por defecto en `4200`.

## Dependencias no identificadas como usadas

En las dependencias declaradas del frontend aparecen Chart.js, ng2-charts y ng-bootstrap, pero no se encontraron referencias a ellas en el código fuente actual. No se identificó una librería JWT externa en `pom.xml`; la creación y validación del token de acceso se realiza mediante clases propias del backend.
