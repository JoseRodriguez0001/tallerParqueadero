package com.parqueadero.datos.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.dominio.modelo.TipoVehiculo;

class TipoVehiculoRepositoryJdbcTest extends IntegracionBD {

    private TipoVehiculoRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new TipoVehiculoRepositoryJdbc(gestor);
    }

    @Test
    @DisplayName("Encuentra el tipo CARRO con su id, nombre y estado activo")
    void encuentraCarro() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarPorCodigo("CARRO")).hasValueSatisfying(tipo -> {
                assertThat(tipo.getId()).isNotNull();
                assertThat(tipo.getCodigo()).isEqualTo("CARRO");
                assertThat(tipo.getNombre()).isEqualTo("Automóvil");
                assertThat(tipo.estaActivo()).isTrue();
            }));
    }

    @Test
    @DisplayName("Un código inexistente devuelve vacío, no null ni excepción")
    void inexistenteDevuelveVacio() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarPorCodigo("AVION")).isEmpty());
    }

    @Test
    @DisplayName("buscarPorCodigo devuelve también los tipos inactivos: el dominio decide si se usan")
    void buscarPorCodigoIncluyeInactivos() {
        enTransaccionRevertida(() -> {
            fila("INSERT INTO tipo_vehiculo (codigo, nombre, activo) VALUES (?, ?, false) RETURNING id",
                    "BICI", "Bicicleta");

            assertThat(repositorio.buscarPorCodigo("BICI"))
                    .hasValueSatisfying(tipo -> assertThat(tipo.estaActivo()).isFalse());
        });
    }

    @Test
    @DisplayName("listarActivos incluye MOTO y CARRO pero no los tipos inactivos")
    void listarActivosExcluyeInactivos() {
        enTransaccionRevertida(() -> {
            fila("INSERT INTO tipo_vehiculo (codigo, nombre, activo) VALUES (?, ?, false) RETURNING id",
                    "BICI", "Bicicleta");

            List<TipoVehiculo> activos = repositorio.listarActivos();

            assertThat(activos).extracting(TipoVehiculo::getCodigo)
                    .contains("MOTO", "CARRO")
                    .doesNotContain("BICI");
            assertThat(activos).allMatch(TipoVehiculo::estaActivo);
            assertThat(activos).extracting(TipoVehiculo::getNombre).isSorted();
        });
    }
}
