package com.parqueadero.datos.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.parqueadero.datos.repositorio.UsuarioRepository;
import com.parqueadero.dominio.modelo.Rol;
import com.parqueadero.dominio.modelo.Usuario;

public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private static final String BUSCAR_POR_NOMBRE_USUARIO =
            "SELECT id, nombre_usuario, nombre, contrasena_hash, rol, activo "
            + "FROM usuario WHERE nombre_usuario = ?";

    private final GestorTransaccionesJdbc gestor;

    public UsuarioRepositoryJdbc(GestorTransaccionesJdbc gestor) {
        this.gestor = gestor;
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        try (PreparedStatement ps = gestor.conexionActual().prepareStatement(BUSCAR_POR_NOMBRE_USUARIO)) {
            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar el usuario " + nombreUsuario + ".", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return Usuario.reconstruir(
                rs.getInt("id"),
                rs.getString("nombre_usuario"),
                rs.getString("nombre"),
                rs.getString("contrasena_hash"),
                Rol.valueOf(rs.getString("rol")),
                rs.getBoolean("activo"));
    }
}
