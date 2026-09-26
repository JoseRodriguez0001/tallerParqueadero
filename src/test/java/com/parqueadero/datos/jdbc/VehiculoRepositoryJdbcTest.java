package com.parqueadero.datos.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.datos.repositorio.VehiculoRepository;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Vehiculo;

class VehiculoRepositoryJdbcTest extends IntegracionBD {

    private VehiculoRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new VehiculoRepositoryJdbc(gestor);
    }

    @Test
    @DisplayName("guardar asigna un id y luego buscarPorPlaca lo encuentra con su tipo completo")
    void guardarYBuscar() {
        enTransaccionRevertida(() -> {
            TipoVehiculo carro = tipo("CARRO");

            Vehiculo guardado = repositorio.guardar(new Vehiculo("ZZZ999", carro));

            assertThat(guardado.getId()).isNotNull();
            assertThat(guardado.getPlaca()).isEqualTo("ZZZ999");
            assertThat(repositorio.buscarPorPlaca("ZZZ999")).hasValueSatisfying(encontrado -> {
                assertThat(encontrado.getId()).isEqualTo(guardado.getId());
                assertThat(encontrado.getPlaca()).isEqualTo("ZZZ999");
                assertThat(encontrado.getTipoVehiculo().getId()).isEqualTo(carro.getId());
                assertThat(encontrado.getTipoVehiculo().getCodigo()).isEqualTo("CARRO");
                assertThat(encontrado.getTipoVehiculo().getNombre()).isEqualTo("Automóvil");
                assertThat(encontrado.getTipoVehiculo().estaActivo()).isTrue();
            });
        });
    }

    @Test
    @DisplayName("Una placa inexistente devuelve vacío, no null ni excepción")
    void inexistenteDevuelveVacio() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarPorPlaca("NOEXISTE1")).isEmpty());
    }

    @Test
    @DisplayName("listarTodos incluye los vehículos insertados, ordenados por placa")
    void listarTodosOrdenadoPorPlaca() {
        enTransaccionRevertida(() -> {
            insertarVehiculo("ZZZ999", "CARRO");
            insertarVehiculo("ZZA111", "MOTO");

            List<Vehiculo> todos = repositorio.listarTodos();

            assertThat(todos).extracting(Vehiculo::getPlaca)
                    .contains("ZZA111", "ZZZ999")
                    .isSorted();
            assertThat(todos).filteredOn(v -> v.getPlaca().equals("ZZA111"))
                    .singleElement()
                    .satisfies(moto -> assertThat(moto.getTipoVehiculo().getNombre()).isEqualTo("Motocicleta"));
        });
    }

    @Test
    @DisplayName("Guardar un vehículo que ya tiene id es un error de programación")
    void guardarVehiculoConId() {
        enTransaccionRevertida(() -> {
            Vehiculo existente = insertarVehiculo("ZZZ999", "CARRO");

            assertThatThrownBy(() -> repositorio.guardar(existente))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El vehículo ya fue guardado.");
        });
    }
}
