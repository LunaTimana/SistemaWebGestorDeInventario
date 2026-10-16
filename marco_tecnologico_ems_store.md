# Marco tecnológico

- **Figma como herramienta de diseño y prototipado:** Figma es una herramienta de diseño colaborativo que permite crear interfaces, prototipos y representaciones visuales de sistemas digitales. En el proyecto de EMS Store se utilizó para diseñar las principales interfaces de la solución propuesta, permitiendo representar la distribución de los módulos, pantallas y elementos que utilizarán los usuarios. Estos prototipos sirven como referencia para el desarrollo del sistema.

- **Angular como framework de frontend:** Angular es un framework de código abierto basado en TypeScript que permite construir aplicaciones web de una sola página (SPA) organizadas en módulos y componentes. En el proyecto se utilizará para desarrollar la interfaz del sistema, con módulos independientes para inventario, movimientos, alertas, reportes y administración de usuarios. Además, sus formularios reactivos, su enrutador y sus guardas de ruta permitirán validar los datos ingresados y restringir el acceso a cada pantalla según el perfil del usuario (Administrador, Almacén y Ventas).

- **TypeScript como lenguaje del frontend:** TypeScript es un lenguaje que extiende JavaScript con tipado estático, lo que ayuda a detectar errores durante el desarrollo y facilita el mantenimiento del código. Es el lenguaje base de Angular y se empleará en toda la capa de presentación.

- **HTML5, Bootstrap 5 y SCSS para la interfaz:** HTML5 define la estructura de las pantallas, Bootstrap 5 proporciona componentes y una cuadrícula adaptable que permiten una interfaz responsiva (RNF03), y SCSS facilita la organización y reutilización de los estilos personalizados. Se complementarán con ng-bootstrap (o Angular Material) para componentes como ventanas modales, paginación y listas desplegables.

- **ng2-charts (Chart.js) para gráficos:** librería que permite mostrar indicadores visuales en el dashboard y en los reportes, como el nivel de stock y los productos con mayor rotación.

- **Spring Boot como framework de backend:** Spring Boot es un framework de Java que simplifica la creación de aplicaciones y servicios web. En el proyecto se utilizará para construir la API REST que contendrá la lógica de negocio del sistema, como el registro de productos y variantes, el control de entradas y salidas, la generación de alertas de stock mínimo y la emisión de reportes. Las operaciones que modifican el stock se ejecutarán dentro de transacciones para mantener la información consistente.

- **Java como lenguaje del backend:** Java es un lenguaje de programación orientado a objetos, robusto y ampliamente utilizado en aplicaciones empresariales. Se empleará en la capa de servicios y de acceso a datos del sistema.

- **Spring Data JPA (Hibernate) para el acceso a datos:** permite relacionar las clases del sistema con las tablas de la base de datos y realizar consultas, filtros y paginación sin escribir sentencias SQL para cada operación. Facilitará la gestión de las entidades roles, usuarios, productos, variantes de producto, proveedores y movimientos de inventario.

- **Spring Security, JWT y BCrypt para la seguridad:** Spring Security controla la autenticación y la autorización por rol; JWT (JSON Web Token) permite identificar al usuario en cada solicitud a la API; y BCrypt almacena las contraseñas mediante hash. En conjunto responden al requerimiento de seguridad RNF01, junto con el uso de un protocolo seguro (HTTPS) para las comunicaciones.

- **Bean Validation para la validación de datos:** conjunto de anotaciones que verifican en el servidor que los datos recibidos sean correctos, por ejemplo, cantidades positivas o campos obligatorios, antes de registrarlos en la base de datos.

- **springdoc-openapi (Swagger UI) para la documentación de la API:** genera automáticamente la documentación de los servicios del sistema y permite probarlos desde el navegador. Servirá como documentación técnica de la solución y como apoyo para las pruebas funcionales.

- **Apache POI y JasperReports (u OpenPDF) para reportes y migración:** Apache POI permite leer y generar archivos de Excel. Se utilizará para importar los datos del inventario actual hacia la nueva base de datos, validando duplicados y datos incompletos, y para exportar reportes. JasperReports (u OpenPDF) permitirá generar reportes en formato PDF.

- **MySQL como sistema de gestión de base de datos:** MySQL es un sistema de gestión de bases de datos relacionales que permite almacenar y organizar información mediante tablas relacionadas. En el proyecto se utilizará para almacenar productos, existencias y movimientos de inventario, con restricciones de unicidad para el código de cada variante e índices que permitan consultas en menos de dos segundos (RNF02).

- **MySQL Workbench para el modelado de la base de datos:** herramienta gráfica que permite diseñar, visualizar y documentar el modelo entidad-relación y generar el script SQL del esquema, el cual se mantendrá versionado en el repositorio del proyecto.

- **Microsoft Excel como fuente de datos actual:** Excel es la herramienta utilizada actualmente por EMS Store para registrar manualmente productos y existencias. Aunque no forma parte de la solución final, constituye una fuente de información importante para el proyecto, ya que sus datos deberán ser revisados, depurados y posteriormente migrados hacia la nueva base de datos. Durante este proceso se deberán tener en cuenta posibles inconsistencias, duplicidades y datos incompletos.

- **Git y GitHub para el control de versiones:** permiten trabajar de forma colaborativa en el código del sistema, organizar el trabajo por ramas según cada módulo y conservar el historial de cambios del equipo.

- **Postman y Swagger UI para pruebas:** herramientas para enviar solicitudes a la API y verificar sus respuestas. Con ellas se realizarán las pruebas funcionales del sistema, cuyos resultados se documentarán en una matriz de casos de prueba.

- **Maven (o Gradle) para la gestión del proyecto:** herramienta que administra las dependencias y la construcción de la aplicación Java.

- **Visual Studio Code e IntelliJ IDEA como entornos de desarrollo:** Visual Studio Code se utilizará para el desarrollo del frontend en Angular, e IntelliJ IDEA para el backend en Spring Boot.

- **Docker y Docker Compose para el entorno de ejecución:** permiten ejecutar la aplicación, la API y la base de datos en contenedores, de modo que todos los integrantes del equipo trabajen con el mismo entorno y se facilite el despliegue del sistema.

- **Navegador web:** el navegador web será el medio de acceso a la aplicación, debido a que el alcance definido corresponde a un sistema web. A través del navegador, los usuarios podrán acceder a las diferentes funcionalidades del sistema, como la consulta de productos, la gestión del inventario y el registro de movimientos.
