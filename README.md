# SistemaWebGestorDeInventario
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
