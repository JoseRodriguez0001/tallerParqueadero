# Sistema de Gestión de Parqueadero

Prototipo del taller **Arquitectura por capas**: una aplicación de escritorio (Swing) en Java puro, sin frameworks, con persistencia en PostgreSQL mediante JDBC.

## Requisitos

| Herramienta | Versión                       |
| ----------- | ------------------------------ |
| JDK         | 17 o superior                  |
| Maven       | 3.9 o superior                 |
| PostgreSQL  | 14 o superior (probado con 17) |

## Instalación y ejecución

**1. Crear la base de datos y cargar el script**

```bash
psql -U postgres -c "CREATE DATABASE parqueadero;"
psql -U postgres -d parqueadero -f documentos/script/parqueadero.sql
```

El script crea las tablas y carga los datos iniciales:

- Tipos de vehículo: **Motocicleta** ($800 por hora) y **Automóvil** ($1.800 por hora).
- Usuarios de demostración: `admin` y `personal`.

> El script empieza con `DROP TABLE`. Si lo vuelves a ejecutar, borra los datos existentes.

**2. Configurar la conexión**

Copia la plantilla y escribe tu contraseña de PostgreSQL:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

```properties
db.url=jdbc:postgresql://localhost:5432/parqueadero
db.usuario=postgres
db.contrasena=TU_CONTRASEÑA
```

**3. Compilar**

```bash
mvn clean package
```

Genera `target/parqueadero-1.0.0.jar`, un único archivo ejecutable que ya incluye el driver de PostgreSQL.

**4. Ejecutar**

```bash
java -jar target/parqueadero-1.0.0.jar
```

Desde un IDE, ejecuta la clase `com.parqueadero.config.Aplicacion`.

> Para cambiar la conexión sin recompilar, coloca un `db.properties` en la carpeta desde donde ejecutas el comando. Ese archivo tiene prioridad sobre el incluido en el `.jar`.

## Funcionalidades del prototipo

La ventana tiene una pestaña por funcionalidad:

| Pestaña                    | Caso de uso                                                                                                            |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **Vehículos**        | CU-04 Registrar vehículo y CU-07 Consultar vehículos (listas «En el parqueadero» y «Registrados»)                |
| **Registrar ingreso** | CU-03 Registrar ingreso. Si la placa no está registrada, se elige el tipo y el vehículo se registra en el mismo paso |
| **Registrar salida**  | CU-05 Registrar salida: calcula el valor y deja la estadía pendiente de pago                                          |
| **Pago en caja**      | CU-01 Consultar valor y CU-06 Registrar pago en caja (versión mínima)                                                |

Cobro: toda hora iniciada se cobra completa, con un mínimo de una hora.

El diseño completo (pago en línea, gestión de tarifas, tipos y usuarios, reportes) está en el documento de diseño. No forma parte del prototipo. Como el prototipo no tiene inicio de sesión, los pagos en caja se registran a nombre del usuario de demostración `personal`.

### Recorrido de prueba sugerido

1. **Registrar ingreso:** placa `ABC123`, tipo *Automóvil* → queda **Dentro**.
2. **Pago en caja → Consultar valor:** muestra el valor **estimado** a la hora actual. Todavía no se puede pagar.
3. **Registrar salida:** `ABC123` → muestra el valor a pagar y la estadía queda **Pendiente de pago**. Por ejemplo, 2 h 40 min se cobran como 3 horas: $5.400.
4. **Registrar ingreso** de `ABC123` otra vez → se rechaza, porque tiene una estadía sin pagar.
5. **Pago en caja:** consultar `ABC123` y **Registrar pago** → la estadía queda **Cerrada**.
6. **Registrar ingreso** de `ABC123` → ahora sí se acepta.
7. **Vehículos:** revisar las listas «En el parqueadero» y «Registrados».

### Extensibilidad

Tipos de vehículo y tarifas son **datos**: agregar un tipo (por ejemplo, camioneta) o cambiar una tarifa no requiere modificar el código. El final de `documentos/script/parqueadero.sql` tiene ejemplos listos para ejecutar.

## Arquitectura

Todo el código está en `src/main/java/com/parqueadero`, organizado en capas. Las dependencias van siempre hacia abajo: ninguna capa conoce a una superior.

```
config/          Aplicacion: punto de arranque; crea y conecta los objetos de todas las capas
presentacion/    vista (paneles Swing), controlador y componentes (tablas, formatos, selector)
negocio/         servicio (casos de uso), dto y mapper
dominio/         modelo (entidades con sus reglas) y cobro (estrategia de cálculo del valor)
datos/           repositorio (interfaces), jdbc (implementaciones), transaccion, pasarela (interfaz)
comun/           NegocioException
```

- **Presentación:** usa solo servicios y DTO; nunca ve el dominio ni la base de datos.
- **Transacciones:** cada caso de uso se ejecuta como una sola transacción (`GestorTransacciones`).

## Problemas frecuentes

| Mensaje al iniciar                                   | Solución                                                                                        |
| ---------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| *No se encontró db.properties*                    | Realiza el paso 2 y vuelve a compilar, o coloca el archivo junto a donde ejecutas el`.jar`     |
| *Error de acceso a la base de datos*               | Verifica que PostgreSQL esté encendido y que la URL, el usuario y la contraseña sean correctos |
| *La base de datos no tiene las tablas del sistema* | Ejecuta el script del paso 1 sobre la base de datos configurada                                  |

## Autores

- Jose Alejandro Rodríguez Rincones – 2023114029
- Laura Sofía Perez Hernandez – 2023114022

Universidad del Magdalena · Arquitectura de Software · Docente: Carlos Nelson Henriquez Miranda
