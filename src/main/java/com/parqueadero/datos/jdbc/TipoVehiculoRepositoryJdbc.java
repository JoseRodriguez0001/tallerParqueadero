package com.parqueadero.datos.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.dominio.modelo.TipoVehiculo;

public class TipoVehiculoRepositoryJdbc implements TipoVehiculoRepository {

    private static final String BUSCAR_POR_CODIGO = "SELECT id, codigo, nombre, activo FROM tipo_vehiculo WHERE codigo = ?";

    private static final String LISTAR_ACTIVOS = "SELECT id, codigo, nombre, activo FROM tipo_vehiculo WHERE activo ORDER BY nombre";

    private final GestorTransaccionesJdbc gestor;

    public TipoVehiculoRepositoryJdbc(GestorTransaccionesJdbc gestor) {
        this.gestor = gestor;
    }

    @Override
    public Optional<TipoVehiculo> buscarPorCodigo(String codigo) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(BUSCAR_POR_CODIGO)) {
            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar el tipo de vehículo " + codigo + ".", e);
        }
    }

    @Override
    public List<TipoVehiculo> listarActivos() {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(LISTAR_ACTIVOS);
                ResultSet rs = ps.executeQuery()) {

            List<TipoVehiculo> tipos = new ArrayList<>();
            while (rs.next()) {
                tipos.add(mapear(rs));
            }
            return tipos;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar los tipos de vehículo activos.", e);
        }
    }

    private TipoVehiculo mapear(ResultSet rs) throws SQLException {
        return TipoVehiculo.reconstruir(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getBoolean("activo"));
    }
}
