package com.parqueadero.datos.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.parqueadero.datos.repositorio.EstadiaRepository;
import com.parqueadero.dominio.modelo.CanalPago;
import com.parqueadero.dominio.modelo.EstadoEstadia;
import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.dominio.modelo.Pago;
import com.parqueadero.dominio.modelo.Rol;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Usuario;
import com.parqueadero.dominio.modelo.Vehiculo;

public class EstadiaRepositoryJdbc implements EstadiaRepository {

    private static final String GUARDAR_ESTADIA = "INSERT INTO estadia (vehiculo_id, tarifa_id, fecha_ingreso, fecha_salida, valor_total, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String ACTUALIZAR_ESTADIA = "UPDATE estadia SET fecha_salida = ?, valor_total = ?, estado = ? WHERE id = ?";

    private static final String GUARDAR_PAGO = "INSERT INTO pago (estadia_id, valor, fecha_pago, canal, referencia, usuario_id) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    // Estadía con su vehículo, tipo, tarifa y, si existe, su pago y el empleado que
    // lo recibió
    private static final String SELECCIONAR_ESTADIAS = "SELECT e.id, e.fecha_ingreso, e.fecha_salida, e.valor_total, e.estado, "
            + "       v.id AS vehiculo_id, v.placa, "
            + "       tv.id AS tipo_id, tv.codigo AS tipo_codigo, tv.nombre AS tipo_nombre, tv.activo AS tipo_activo, "
            + "       t.id AS tarifa_id, t.valor_hora, t.vigente_desde, t.vigente_hasta, "
            + "       p.id AS pago_id, p.valor AS pago_valor, p.fecha_pago, p.canal, p.referencia, "
            + "       u.id AS usuario_id, u.nombre_usuario, u.nombre AS usuario_nombre, "
            + "       u.contrasena_hash, u.rol, u.activo AS usuario_activo "
            + "FROM estadia e "
            + "JOIN vehiculo v       ON v.id = e.vehiculo_id "
            + "JOIN tipo_vehiculo tv ON tv.id = v.tipo_vehiculo_id "
            + "JOIN tarifa t         ON t.id = e.tarifa_id "
            + "LEFT JOIN pago p      ON p.estadia_id = e.id "
            + "LEFT JOIN usuario u   ON u.id = p.usuario_id ";

    private static final String BUSCAR_ACTIVA_POR_PLACA = SELECCIONAR_ESTADIAS
            + "WHERE v.placa = ? AND e.estado <> 'CERRADA'";

    private static final String LISTAR_ACTIVAS = SELECCIONAR_ESTADIAS
            + "WHERE e.estado <> 'CERRADA' ORDER BY e.fecha_ingreso";

    private final GestorTransaccionesJdbc gestor;

    public EstadiaRepositoryJdbc(GestorTransaccionesJdbc gestor) {
        this.gestor = gestor;
    }

    @Override
    public Optional<Estadia> buscarActivaPorPlaca(String placa) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(BUSCAR_ACTIVA_POR_PLACA)) {
            ps.setString(1, placa);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar la estadía activa de la placa " + placa + ".", e);
        }
    }

    @Override
    public List<Estadia> listarActivas() {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(LISTAR_ACTIVAS);
                ResultSet rs = ps.executeQuery()) {

            List<Estadia> estadias = new ArrayList<>();
            while (rs.next()) {
                estadias.add(mapear(rs));
            }
            return estadias;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar las estadías activas.", e);
        }
    }

    @Override
    public Estadia guardar(Estadia estadia) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(GUARDAR_ESTADIA)) {
            ps.setInt(1, estadia.getVehiculo().getId());
            ps.setInt(2, estadia.getTarifa().getId());
            ps.setObject(3, estadia.getFechaIngreso());
            ps.setObject(4, estadia.getFechaSalida().orElse(null));
            ps.setBigDecimal(5, estadia.getValorTotal().orElse(null));
            ps.setString(6, estadia.getEstado().name());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int idGenerado = rs.getInt("id");

                return Estadia.reconstruir(idGenerado, estadia.getVehiculo(), estadia.getTarifa(),
                        estadia.getFechaIngreso(), estadia.getFechaSalida().orElse(null),
                        estadia.getValorTotal().orElse(null), estadia.getEstado(), estadia.getPago().orElse(null));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error al guardar la estadía del vehículo " + estadia.getVehiculo().getPlaca() + ".", e);
        }
    }

    @Override
    public void actualizar(Estadia estadia) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(ACTUALIZAR_ESTADIA)) {
            ps.setObject(1, estadia.getFechaSalida().orElse(null));
            ps.setBigDecimal(2, estadia.getValorTotal().orElse(null));
            ps.setString(3, estadia.getEstado().name());
            ps.setInt(4, estadia.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al actualizar la estadía " + estadia.getId() + ".", e);
        }

        // Un pago sin id todavía no existe en la base de datos (como máximo uno por
        // estadía)
        Optional<Pago> pagoNuevo = estadia.getPago().filter(pago -> pago.getId() == null);
        pagoNuevo.ifPresent(pago -> guardarPago(estadia.getId(), pago));
    }

    private void guardarPago(int estadiaId, Pago pago) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(GUARDAR_PAGO)) {
            ps.setInt(1, estadiaId);
            ps.setBigDecimal(2, pago.getValor());
            ps.setObject(3, pago.getFechaPago());
            ps.setString(4, pago.getCanal().name());
            ps.setString(5, pago.getReferencia().orElse(null));
            ps.setObject(6, pago.getEmpleado().map(Usuario::getId).orElse(null));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al guardar el pago de la estadía " + estadiaId + ".", e);
        }
    }

    private Estadia mapear(ResultSet rs) throws SQLException {
        TipoVehiculo tipo = TipoVehiculo.reconstruir(
                rs.getInt("tipo_id"),
                rs.getString("tipo_codigo"),
                rs.getString("tipo_nombre"),
                rs.getBoolean("tipo_activo"));

        Vehiculo vehiculo = Vehiculo.reconstruir(rs.getInt("vehiculo_id"), rs.getString("placa"), tipo);

        // La tarifa es del mismo tipo que el vehículo: Estadia.iniciar lo garantiza
        Tarifa tarifa = Tarifa.reconstruir(
                rs.getInt("tarifa_id"),
                tipo,
                rs.getBigDecimal("valor_hora"),
                rs.getObject("vigente_desde", LocalDateTime.class),
                rs.getObject("vigente_hasta", LocalDateTime.class));

        return Estadia.reconstruir(
                rs.getInt("id"),
                vehiculo,
                tarifa,
                rs.getObject("fecha_ingreso", LocalDateTime.class),
                rs.getObject("fecha_salida", LocalDateTime.class),
                rs.getBigDecimal("valor_total"),
                EstadoEstadia.valueOf(rs.getString("estado")),
                mapearPago(rs));
    }

    // Los LEFT JOIN devuelven null en las columnas del pago cuando la estadía aún
    // no se ha pagado
    private Pago mapearPago(ResultSet rs) throws SQLException {
        Integer pagoId = rs.getObject("pago_id", Integer.class);
        if (pagoId == null) {
            return null;
        }

        Usuario empleado = null;
        Integer usuarioId = rs.getObject("usuario_id", Integer.class);
        if (usuarioId != null) {
            empleado = Usuario.reconstruir(
                    usuarioId,
                    rs.getString("nombre_usuario"),
                    rs.getString("usuario_nombre"),
                    rs.getString("contrasena_hash"),
                    Rol.valueOf(rs.getString("rol")),
                    rs.getBoolean("usuario_activo"));
        }

        return Pago.reconstruir(
                pagoId,
                rs.getBigDecimal("pago_valor"),
                rs.getObject("fecha_pago", LocalDateTime.class),
                CanalPago.valueOf(rs.getString("canal")),
                rs.getString("referencia"),
                empleado);
    }
}
