package com.parqueadero.datos.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GestorTransaccionesJdbcTest {

    private ConexionBD conexionBD;
    private Connection conexion;
    private GestorTransaccionesJdbc gestor;

    @BeforeEach
    void setUp() throws SQLException {
        conexionBD = mock(ConexionBD.class);
        conexion   = mock(Connection.class);
        when(conexionBD.abrir()).thenReturn(conexion);
        gestor = new GestorTransaccionesJdbc(conexionBD);
    }

    @Test
    @DisplayName("Confirma la transacción y devuelve el resultado si la operación termina bien")
    void confirmaSiTerminaBien() throws SQLException {
        String resultado = gestor.ejecutar(() -> "ok");

        assertThat(resultado).isEqualTo("ok");
        verify(conexion).setAutoCommit(false);
        verify(conexion).commit();
        verify(conexion, never()).rollback();
        verify(conexion).close();
    }

    @Test
    @DisplayName("Deshace la transacción y propaga la misma excepción si la operación falla")
    void deshaceSiFalla() throws SQLException {
        RuntimeException error = new RuntimeException("regla violada");

        assertThatThrownBy(() -> gestor.ejecutar(() -> { throw error; }))
                .isSameAs(error);

        verify(conexion).rollback();
        verify(conexion, never()).commit();
        verify(conexion).close();
    }

    @Test
    @DisplayName("Dentro de la operación, los repositorios obtienen la misma conexión")
    void exponeLaConexionDentroDeLaOperacion() {
        Connection vista = gestor.ejecutar(gestor::conexionActual);

        assertThat(vista).isSameAs(conexion);
    }

    @Test
    @DisplayName("Fuera de una transacción no hay conexión disponible")
    void sinTransaccionNoHayConexion() {
        assertThatThrownBy(gestor::conexionActual)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Libera la conexión al terminar, incluso si la operación falla")
    void liberaLaConexionAlTerminar() {
        assertThatThrownBy(() -> gestor.ejecutar(() -> { throw new RuntimeException(); }));

        assertThatThrownBy(gestor::conexionActual)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Una operación anidada se une a la transacción en curso")
    void operacionAnidadaUsaLaMismaTransaccion() throws SQLException {
        gestor.ejecutar(() -> gestor.ejecutar(() -> "anidada"));

        verify(conexionBD, times(1)).abrir();
        verify(conexion, times(1)).commit();
    }

    @Test
    @DisplayName("Un error de conexión se reporta como error de acceso a datos")
    void errorDeConexion() throws SQLException {
        when(conexionBD.abrir()).thenThrow(new SQLException("sin servidor"));

        assertThatThrownBy(() -> gestor.ejecutar(() -> "x"))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(SQLException.class);
    }
}
