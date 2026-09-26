package com.parqueadero.datos.jdbc;

import com.parqueadero.datos.transaccion.GestorTransacciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Implementación JDBC de {@link GestorTransacciones}.
 *
 * Abre una conexión por transacción y la guarda en un {@link ThreadLocal} para que
 * todos los repositorios que participan usen la misma conexión sin recibirla como
 * parámetro. Así la capa de negocio nunca ve una {@link Connection}.
 *
 * Reglas para los repositorios JDBC:
 * <ul>
 *   <li>Obtienen la conexión con {@link #conexionActual()}.</li>
 *   <li>Nunca la cierran ni llaman a commit o rollback: eso lo hace este gestor.</li>
 *   <li>Sí cierran sus propios PreparedStatement y ResultSet (try-with-resources).</li>
 * </ul>
 */
public class GestorTransaccionesJdbc implements GestorTransacciones {

    private final ConexionBD conexionBD;
    private final ThreadLocal<Connection> conexionEnCurso = new ThreadLocal<>();

    public GestorTransaccionesJdbc(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        // Si ya hay una transacción en curso (un servicio que llama a otro),
        // la operación se une a ella en lugar de abrir otra.
        if (conexionEnCurso.get() != null) {
            return operacion.get();
        }

        try (Connection conexion = conexionBD.abrir()) {
            conexion.setAutoCommit(false);
            conexionEnCurso.set(conexion);
            try {
                T resultado = operacion.get();
                conexion.commit();
                return resultado;
            } catch (RuntimeException | Error e) {
                deshacer(conexion, e);
                throw e;
            } finally {
                conexionEnCurso.remove();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error de acceso a la base de datos.", e);
        }
    }

    /**
     * Conexión de la transacción en curso.
     *
     * @throws IllegalStateException si se llama fuera de {@link #ejecutar(Supplier)}
     */
    public Connection conexionActual() {
        Connection conexion = conexionEnCurso.get();
        if (conexion == null) {
            throw new IllegalStateException(
                    "No hay una transacción en curso: el repositorio debe usarse dentro de GestorTransacciones.ejecutar().");
        }
        return conexion;
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    private static void deshacer(Connection conexion, Throwable causa) {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);   // no ocultar el error original
        }
    }
}
