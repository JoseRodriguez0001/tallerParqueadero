# Guía de implementación — división por entidad

Prototipo del Sistema de Gestión de Parqueadero. Esta guía reparte el trabajo entre los dos integrantes y fija los **contratos** (interfaces) que permiten trabajar en paralelo sin bloquearse.

Referencia obligatoria: `Diseno_Sistema_Parqueadero.docx` (secciones 6, 7 y 8).

---

## 1. Reparto

|                                  | **Integrante A — Estadía**                                              | **Integrante B — Vehículo y catálogo**                                                                                              |
| -------------------------------- | ------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| **Entidades**              | `Estadia`, `Pago`, `EstadoEstadia`, `CanalPago`, `Usuario`, `Rol`   | `Vehiculo`, `TipoVehiculo`, `Tarifa`                                                                                                   |
| **Cobro**                  | `PoliticaCobro`, `CobroPorHoraIniciada`                                     | —                                                                                                                                           |
| **Repositorios JDBC**      | `EstadiaRepositoryJdbc` (incluye el pago), `UsuarioRepositoryJdbc`          | `VehiculoRepositoryJdbc`, `TipoVehiculoRepositoryJdbc`, `TarifaRepositoryJdbc`                                                         |
| **Servicios**              | `EstadiaService`                                                              | `VehiculoService`, `TipoVehiculoService`                                                                                                 |
| **DTO / mapper**           | `EstadiaDTO`, `EstadiaMapper`                                               | `VehiculoDTO`, `TipoVehiculoDTO` y sus mappers                                                                                           |
| **Controladores y vistas** | `EstadiaController`: ingreso, salida, pago en caja, vista "En el parqueadero" | `VehiculoController`: registrar vehículo, vista "Registrados"; **base web** (servidor, plantilla común, manejo de errores, inicio) |
| **Casos de uso**           | CU-03 (con el extend a CU-04), CU-05, CU-06, CU-07 vista "En el parqueadero"    | CU-04 independiente, CU-07 vista "Registrados"                                                                                               |
| **Infraestructura**        | Fase 0 completa,`Aplicacion`                                                  | —                                                                                                                                           |

Reglas del reparto:

- `Estadia` y `Pago` van juntos: el pago lo crea la estadía y se guarda por `EstadiaRepository` (composición).
- `TipoVehiculo` y `Tarifa` van juntos: se crean y se consultan juntos (RN-15, RN-01).
- Cada integrante recorre **todas las capas** de sus entidades. Ambos deben poder explicar el sistema completo en la sustentación.

---

## 2. Fase 0 — base compartida (Integrante A, antes de dividir)

Nadie empieza su parte hasta que la Fase 0 esté en `main`.

1. Renombrar `com.example` a `com.parqueadero`, borrar el ejemplo de `Product` y actualizar `groupId`, `artifactId` y `mainClass` en el `pom.xml`.
2. Crear la estructura de paquetes (sección 3).
3. `NegocioException`, los enums y las entidades con sus atributos, constructores y getters. La lógica de negocio se completa después, cada quien la suya.
4. **Todas las interfaces de la sección 4**, exactamente como están.
5. `ConexionBD` + `GestorTransaccionesJdbc` funcionando, porque los dos los necesitan para probar sus repositorios.
6. `db.properties.example` y `db.properties` agregado al `.gitignore`.
7. `Aplicacion` con un `main` que arranca y se conecta a la BD.
8. `mvn test` en verde → commit → push a `main`.

---

## 3. Estructura de paquetes

```
com.parqueadero
├── config            Aplicacion
├── comun             NegocioException
├── presentacion
│   ├── controlador   ControladorBase, VehiculoController, EstadiaController, InicioController
│   └── vista         Vista (plantilla común), vistas por página
├── negocio
│   ├── servicio      interfaces de servicio
│   │   └── impl      implementaciones
│   ├── dto
│   └── mapper
├── datos
│   ├── repositorio   interfaces de repositorio
│   ├── transaccion   GestorTransacciones
│   ├── pasarela      PasarelaPago, PasarelaPagoSimulada (fuera del alcance del prototipo)
│   └── jdbc          ConexionBD, GestorTransaccionesJdbc, *RepositoryJdbc
└── dominio
    ├── modelo        entidades y enums
    └── cobro         PoliticaCobro, CobroPorHoraIniciada
```

**Regla de dependencias (sección 8.3 del documento):** la presentación **nunca** importa `dominio` ni `datos`. Se verifica antes de cada Pull Request:

```bash
grep -rn "import com.parqueadero.dominio\|import com.parqueadero.datos" src/main/java/com/parqueadero/presentacion
# debe salir vacío
```

---

## 4. Contratos (no se cambian sin avisar a la otra persona)

### 4.1 Transacciones

```java
public interface GestorTransacciones {
    <T> T ejecutar(Supplier<T> operacion);   // commit si termina bien, rollback si lanza excepción
}
```

**Regla única:** todo método de servicio, incluso los de solo lectura, se ejecuta dentro de `gestor.ejecutar(...)`. Los repositorios JDBC obtienen la conexión con `gestor.conexionActual()` (método de `GestorTransaccionesJdbc`) y **nunca la cierran**; la cierra el gestor.

### 4.2 Repositorios

```java
public interface TipoVehiculoRepository {                                // B
    Optional<TipoVehiculo> buscarPorCodigo(String codigo);
    List<TipoVehiculo> listarActivos();
}

public interface TarifaRepository {                                      // B
    Optional<Tarifa> buscarVigente(int tipoVehiculoId, LocalDateTime fecha);  // intervalo [desde, hasta)
}

public interface VehiculoRepository {                                    // B
    Optional<Vehiculo> buscarPorPlaca(String placa);
    Vehiculo guardar(Vehiculo vehiculo);                                 // devuelve el vehículo con su id
    List<Vehiculo> listarTodos();
}

public interface EstadiaRepository {                                     // A
    Optional<Estadia> buscarActivaPorPlaca(String placa);               // DENTRO o PENDIENTE_PAGO
    List<Estadia> listarActivas();
    Estadia guardar(Estadia estadia);                                    // INSERT (ingreso)
    void actualizar(Estadia estadia);                                    // UPDATE + INSERT del pago si es nuevo
}

public interface UsuarioRepository {                                     // A
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
}
```

### 4.3 Servicios

```java
public interface VehiculoService {                                       // B
    VehiculoDTO registrar(String placa, String codigoTipo);              // CU-04
    List<VehiculoDTO> listarRegistrados();                               // CU-07 "Registrados"
}

public interface TipoVehiculoService {                                   // B
    List<TipoVehiculoDTO> listarActivos();                               // para el <select> de tipos
}

public interface EstadiaService {                                        // A
    EstadiaDTO registrarIngreso(String placa, String codigoTipo);        // CU-03; codigoTipo solo si la placa es nueva (CU-04 extend)
    EstadiaDTO consultarValor(String placa);                             // CU-01 (estimado o definitivo, RN-13)
    EstadiaDTO registrarSalida(String placa);                            // CU-05
    EstadiaDTO registrarPagoEnCaja(String placa);                        // CU-06, a nombre del usuario demo
    List<EstadiaDTO> listarEnParqueadero();                              // CU-07 "En el parqueadero"
}
```

### 4.4 DTOs (solo datos, sin lógica)

```java
public record TipoVehiculoDTO(String codigo, String nombre) {}
public record VehiculoDTO(String placa, String tipo, boolean dentro) {}
public record EstadiaDTO(String placa, String tipo, LocalDateTime fechaIngreso,
                         LocalDateTime fechaSalida, BigDecimal valor, String estado) {}
```

`VehiculoDTO.dentro` lo calcula `VehiculoService` usando `EstadiaRepository.listarActivas()`. Es la única dependencia de B hacia la parte de A, y es solo hacia la interfaz de la Fase 0.

### 4.5 Base web (B la implementa; A la usa)

```java
public abstract class ControladorBase implements HttpHandler {
    protected Map<String, String> leerFormulario(HttpExchange ex);       // application/x-www-form-urlencoded
    protected void responderHtml(HttpExchange ex, String html);
    protected void redirigir(HttpExchange ex, String ruta);
    // handle(...) captura NegocioException y muestra el mensaje en la página
}

public final class Vista {
    public static String pagina(String titulo, String contenido);        // layout común con menú
    public static String escapar(String texto);                          // OBLIGATORIO en todo dato mostrado
}
```

`Vista.escapar` se usa en **todo** texto que venga del usuario o de la BD, como la placa. Si no, un texto como `<script>` escrito en el campo de placa se ejecutaría en el navegador.

### 4.6 Rutas

| Ruta                                      | Dueño | Qué hace                          |
| ----------------------------------------- | ------ | ---------------------------------- |
| `GET /`                                 | B      | Inicio con el menú                |
| `GET /vehiculos` · `POST /vehiculos` | B      | Registrados · Registrar vehículo |
| `GET /estadias`                         | A      | En el parqueadero                  |
| `POST /estadias/ingreso`                | A      | Registrar ingreso                  |
| `POST /estadias/salida`                 | A      | Registrar salida                   |
| `POST /estadias/pago`                   | A      | Pago en caja                       |

---

## 5. Orden de trabajo y dependencias

| Momento                | Integrante A                                                                                | Integrante B                                                                                                                              |
| ---------------------- | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **Vie mañana**  | Fase 0 → push                                                                              | Leer el documento de diseño, ejecutar el script, crear su`db.properties`                                                               |
| **Vie tarde**    | `CobroPorHoraIniciada` + `Estadia` + `Pago` con JUnit (sin BD)                        | **Primero** `TarifaRepositoryJdbc` y `VehiculoRepositoryJdbc` (A los necesita el sábado), luego `TipoVehiculoRepositoryJdbc` |
| **Sáb mañana** | `EstadiaRepositoryJdbc`, `UsuarioRepositoryJdbc`, `EstadiaService`                    | **Primero** la base web (A la necesita el sábado en la tarde), luego `VehiculoService` y `TipoVehiculoService`                 |
| **Sáb tarde**   | `EstadiaController` + vistas                                                              | `VehiculoController` + vistas; revisar el PR de A                                                                                       |
| **Dom**          | Juntos: pruebas de punta a punta (sección 7), correcciones,`.zip`, portada del documento |                                                                                                                                           |

Mientras un repositorio de la otra persona no esté listo, se puede avanzar con **pruebas unitarias con Mockito** sobre la interfaz, que ya existe desde la Fase 0.

---

## 6. Flujo de git

1. Agregar a la compañera como colaboradora: GitHub → *Settings* → *Collaborators*.
2. Ramas: `feat/fase-0` (A), `feat/estadia` (A), `feat/vehiculo-catalogo` (B), `feat/web-base` (B). Nadie hace commit directo en `main`.
3. Antes de abrir un Pull Request: `git pull origin main`, resolver conflictos en la propia rama, `mvn test` en verde, y verificar la regla de dependencias (sección 3).
4. **Todo PR lo revisa la otra persona** antes de fusionarlo.
5. `Aplicacion.java` es el único archivo compartido: cada quien agrega solo sus líneas de ensamblado.
6. Commits: `feat:`, `fix:`, `test:`, `refactor:`, `docs:`.

### Definición de terminado (por PR)

- [ ] Compila y `mvn test` pasa.
- [ ] Las reglas de negocio de sus entidades tienen prueba JUnit, incluidos los casos que deben lanzar `NegocioException`.
- [ ] La presentación no importa `dominio` ni `datos`.
- [ ] Las entidades no tienen setters públicos que permitan saltarse sus reglas.
- [ ] El dinero es `BigDecimal`, y la hora la pone el servidor (`LocalDateTime.now()`), nunca el formulario.
- [ ] Todo dato mostrado en HTML pasa por `Vista.escapar`.

---

## 7. Pruebas de punta a punta (domingo)

Basadas en la sección 6.2 del documento:

1. Registrar el vehículo `ABC123` (Automóvil) → aparece en "Registrados".
2. Registrar el ingreso de `XYZ98D` sin registrarlo antes → pide el tipo y crea el vehículo y la estadía (RN-06).
3. Registrar otro ingreso de `ABC123` mientras está dentro → error RN-08.
4. Registrar la salida de `ABC123` → queda en Pendiente de pago con su valor (verificar RN-17).
5. Intentar un ingreso de `ABC123` en Pendiente de pago → error RN-08.
6. Pago en caja de `ABC123` → Cerrada; en la BD, el pago tiene `usuario_id` del usuario `personal` (RN-18).
7. Nuevo ingreso de `ABC123` → permitido.
8. Pago en caja de una estadía en Dentro → error RN-10.
9. Placa escrita como `abc 123` → se guarda como `ABC123`.
