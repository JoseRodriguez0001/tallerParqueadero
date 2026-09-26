package com.parqueadero.datos.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import com.parqueadero.dominio.modelo.Rol;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Usuario;
import com.parqueadero.dominio.modelo.Vehiculo;

/**
 * Base de las pruebas de integración contra PostgreSQL.
 *
 * - Si la base de datos no está disponible, las pruebas se omiten (no fallan).
 * - Cada prueba corre en una transacción que se revierte al final: no deja datos.
 * - Los datos de apoyo (vehículos, tarifas, usuarios) se leen o insertan con SQL
 *   directo, para no depender de repositorios que aún no existen.
 */
abstract class IntegracionBD {

    protected static GestorTransaccionesJdbc gestor;

    @BeforeAll
    static void conectar() {
        try {
            ConexionBD conexionBD = new ConexionBD();
            try (Connection prueba = conexionBD.abrir()) {
                // solo verifica que el servidor responde
            }
            gestor = new GestorTransaccionesJdbc(conexionBD);
        } catch (Exception e) {
            Assumptions.abort("Base de datos no disponible, se omiten las pruebas de integración: " + e.getMessage());
        }
    }

    // ── Transacción revertida ──────────────────────────────────────────────────

    private static final class Revertir extends RuntimeException {
    }

    protected void enTransaccionRevertida(Runnable prueba) {
        try {
            gestor.ejecutar(() -> {
                prueba.run();
                throw new Revertir();   // fuerza el rollback aunque la prueba pase
            });
        } catch (Revertir esperado) {
            // la transacción se deshizo: la base queda como estaba
        }
    }

    // ── Datos de apoyo ─────────────────────────────────────────────────────────

    protected TipoVehiculo tipo(String codigo) {
        Map<String, Object> fila = fila("SELECT id, codigo, nombre, activo FROM tipo_vehiculo WHERE codigo = ?", codigo);
        return TipoVehiculo.reconstruir((Integer) fila.get("id"), (String) fila.get("codigo"),
                (String) fila.get("nombre"), (Boolean) fila.get("activo"));
    }

    protected Tarifa tarifaVigente(String codigoTipo) {
        TipoVehiculo tipo = tipo(codigoTipo);
        Map<String, Object> fila = fila(
                "SELECT id, valor_hora, vigente_desde FROM tarifa WHERE tipo_vehiculo_id = ? AND vigente_hasta IS NULL",
                tipo.getId());
        return Tarifa.reconstruir((Integer) fila.get("id"), tipo, (BigDecimal) fila.get("valor_hora"),
                ((java.sql.Timestamp) fila.get("vigente_desde")).toLocalDateTime(), null);
    }

    protected Vehiculo insertarVehiculo(String placa, String codigoTipo) {
        TipoVehiculo tipo = tipo(codigoTipo);
        Map<String, Object> fila = fila(
                "INSERT INTO vehiculo (placa, tipo_vehiculo_id) VALUES (?, ?) RETURNING id", placa, tipo.getId());
        return Vehiculo.reconstruir((Integer) fila.get("id"), placa, tipo);
    }

    protected Usuario usuario(String nombreUsuario) {
        Map<String, Object> fila = fila(
                "SELECT id, nombre_usuario, nombre, contrasena_hash, rol, activo FROM usuario WHERE nombre_usuario = ?",
                nombreUsuario);
        return Usuario.reconstruir((Integer) fila.get("id"), (String) fila.get("nombre_usuario"),
                (String) fila.get("nombre"), (String) fila.get("contrasena_hash"),
                Rol.valueOf((String) fila.get("rol")), (Boolean) fila.get("activo"));
    }

    // Primera fila de una consulta como mapa columna → valor; null si no hay filas
    protected Map<String, Object> fila(String sql, Object... parametros) {
        try (PreparedStatement sentencia = gestor.conexionActual().prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                sentencia.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                ResultSetMetaData columnas = resultado.getMetaData();
                Map<String, Object> fila = new HashMap<>();
                for (int i = 1; i <= columnas.getColumnCount(); i++) {
                    fila.put(columnas.getColumnLabel(i), resultado.getObject(i));
                }
                return fila;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en la consulta de apoyo de la prueba: " + sql, e);
        }
    }

    protected static LocalDateTime hora(int hora, int minuto) {
        return LocalDateTime.of(2026, 9, 26, hora, minuto);
    }
}
