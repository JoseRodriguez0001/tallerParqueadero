package com.parqueadero.datos.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.datos.repositorio.UsuarioRepository;
import com.parqueadero.dominio.modelo.Rol;

class UsuarioRepositoryJdbcTest extends IntegracionBD {

    private UsuarioRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new UsuarioRepositoryJdbc(gestor);
    }

    @Test
    @DisplayName("Encuentra al usuario de demostración 'personal' con todos sus datos")
    void encuentraUsuarioPersonal() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarPorNombreUsuario("personal")).hasValueSatisfying(usuario -> {
                assertThat(usuario.getId()).isNotNull();
                assertThat(usuario.getNombreUsuario()).isEqualTo("personal");
                assertThat(usuario.getNombre()).isEqualTo("Empleado del parqueadero");
                assertThat(usuario.getContrasenaHash()).isNotBlank();
                assertThat(usuario.getRol()).isEqualTo(Rol.PERSONAL);
                assertThat(usuario.estaActivo()).isTrue();
            }));
    }

    @Test
    @DisplayName("Convierte el rol de la base de datos al enum (ADMINISTRADOR)")
    void convierteElRol() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarPorNombreUsuario("admin"))
                    .hasValueSatisfying(usuario -> assertThat(usuario.getRol()).isEqualTo(Rol.ADMINISTRADOR)));
    }

    @Test
    @DisplayName("Un nombre de usuario inexistente devuelve vacío, no null ni excepción")
    void inexistenteDevuelveVacio() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarPorNombreUsuario("no-existe")).isEmpty());
    }
}
