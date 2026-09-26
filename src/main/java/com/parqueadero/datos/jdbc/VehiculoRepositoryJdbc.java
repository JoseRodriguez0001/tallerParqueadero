package com.parqueadero.datos.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.parqueadero.datos.repositorio.VehiculoRepository;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Vehiculo;

public class VehiculoRepositoryJdbc implements VehiculoRepository {

    private static final String GUARDAR =
            "INSERT INTO vehiculo (placa, tipo_vehiculo_id) VALUES (?, ?) RETURNING id";

    // Vehículo con su tipo completo
    private static final String SELECCIONAR_VEHICULOS =
            "SELECT v.id, v.placa, "
            + "       tv.id AS tipo_id, tv.codigo AS tipo_codigo, tv.nombre AS tipo_nombre, tv.activo AS tipo_activo "
            + "FROM vehiculo v "
            + "JOIN tipo_vehiculo tv ON tv.id = v.tipo_vehiculo_id ";

    // La placa llega ya normalizada desde el servicio
    private static final String BUSCAR_POR_PLACA =
            SELECCIONAR_VEHICULOS + "WHERE v.placa = ?";

    private static final String LISTAR_TODOS =
            SELECCIONAR_VEHICULOS + "ORDER BY v.placa";

    private final GestorTransaccionesJdbc gestor;

    public VehiculoRepositoryJdbc(GestorTransaccionesJdbc gestor) {
        this.gestor = gestor;
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(BUSCAR_POR_PLACA)) {
            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar el vehículo con placa " + placa + ".", e);
        }
    }

    @Override
    public List<Vehiculo> listarTodos() {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(LISTAR_TODOS);
             ResultSet rs = ps.executeQuery()) {

            List<Vehiculo> vehiculos = new ArrayList<>();
            while (rs.next()) {
                vehiculos.add(mapear(rs));
            }
            return vehiculos;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar los vehículos.", e);
        }
    }

    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        // Solo inserta: no hay caso de uso que modifique un vehículo ya registrado
        if (vehiculo.getId() != null) {
            throw new IllegalArgumentException("El vehículo ya fue guardado.");
        }

        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(GUARDAR)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setInt(2, vehiculo.getTipoVehiculo().getId());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int idGenerado = rs.getInt("id");

                return Vehiculo.reconstruir(idGenerado, vehiculo.getPlaca(), vehiculo.getTipoVehiculo());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al guardar el vehículo " + vehiculo.getPlaca() + ".", e);
        }
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        TipoVehiculo tipo = TipoVehiculo.reconstruir(
                rs.getInt("tipo_id"),
                rs.getString("tipo_codigo"),
                rs.getString("tipo_nombre"),
                rs.getBoolean("tipo_activo"));

        return Vehiculo.reconstruir(rs.getInt("id"), rs.getString("placa"), tipo);
    }
}
