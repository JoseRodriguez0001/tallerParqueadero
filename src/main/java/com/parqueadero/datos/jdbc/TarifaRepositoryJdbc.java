package com.parqueadero.datos.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.parqueadero.datos.repositorio.TarifaRepository;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;

public class TarifaRepositoryJdbc implements TarifaRepository {

    // Misma regla que Tarifa.estaVigenteEn: intervalo semiabierto [vigente_desde,
    // vigente_hasta)
    private static final String BUSCAR_VIGENTE = "SELECT t.id, t.valor_hora, t.vigente_desde, t.vigente_hasta, "
            + "       tv.id AS tipo_id, tv.codigo AS tipo_codigo, tv.nombre AS tipo_nombre, tv.activo AS tipo_activo "
            + "FROM tarifa t "
            + "JOIN tipo_vehiculo tv ON tv.id = t.tipo_vehiculo_id "
            + "WHERE t.tipo_vehiculo_id = ? "
            + "  AND t.vigente_desde <= ? "
            + "  AND (t.vigente_hasta IS NULL OR t.vigente_hasta > ?) "
            + "ORDER BY t.vigente_desde DESC "
            + "LIMIT 1";

    private final GestorTransaccionesJdbc gestor;

    public TarifaRepositoryJdbc(GestorTransaccionesJdbc gestor) {
        this.gestor = gestor;
    }

    @Override
    public Optional<Tarifa> buscarVigente(int tipoVehiculoId, LocalDateTime fecha) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(BUSCAR_VIGENTE)) {
            Timestamp momento = Timestamp.valueOf(fecha);
            ps.setInt(1, tipoVehiculoId);
            ps.setTimestamp(2, momento);
            ps.setTimestamp(3, momento);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al buscar la tarifa vigente del tipo de vehículo " + tipoVehiculoId + ".", e);
        }
    }

    private Tarifa mapear(ResultSet rs) throws SQLException {
        TipoVehiculo tipo = TipoVehiculo.reconstruir(
                rs.getInt("tipo_id"),
                rs.getString("tipo_codigo"),
                rs.getString("tipo_nombre"),
                rs.getBoolean("tipo_activo"));

        Timestamp hasta = rs.getTimestamp("vigente_hasta");

        return Tarifa.reconstruir(
                rs.getInt("id"),
                tipo,
                rs.getBigDecimal("valor_hora"),
                rs.getTimestamp("vigente_desde").toLocalDateTime(),
                hasta == null ? null : hasta.toLocalDateTime());
    }
}
