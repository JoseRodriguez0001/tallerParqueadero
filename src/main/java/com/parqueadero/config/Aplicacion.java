package com.parqueadero.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.parqueadero.datos.jdbc.ConexionBD;
import com.parqueadero.datos.jdbc.GestorTransaccionesJdbc;

/**
 * Punto de arranque. Crea los objetos de cada capa y los conecta (inyección de
 * dependencias manual): es el único lugar que conoce las implementaciones.
 */
public class Aplicacion {

    // Sin inicio de sesión en el prototipo, el pago en caja se registra a nombre de este usuario
    public static final String USUARIO_CAJA = "personal";

    public static void main(String[] args) {

        // ── Datos ──────────────────────────────────────────────────────────────
        ConexionBD conexionBD = new ConexionBD();
        GestorTransaccionesJdbc gestor = new GestorTransaccionesJdbc(conexionBD);

        verificarConexion(gestor);

        // ── Negocio y presentación: se ensamblan a medida que existan ──────────
    }

    private static void verificarConexion(GestorTransaccionesJdbc gestor) {
        int tipos = gestor.ejecutar(() -> {
            try (Statement consulta = gestor.conexionActual().createStatement();
                 ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM tipo_vehiculo WHERE activo")) {
                resultado.next();
                return resultado.getInt(1);
            } catch (SQLException e) {
                throw new IllegalStateException("No se pudo consultar la base de datos.", e);
            }
        });
        System.out.println("Conexión a PostgreSQL correcta: " + tipos + " tipos de vehículo activos.");
    }
}
