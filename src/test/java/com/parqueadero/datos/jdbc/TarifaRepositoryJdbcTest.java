package com.parqueadero.datos.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.datos.repositorio.TarifaRepository;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;

class TarifaRepositoryJdbcTest extends IntegracionBD {

    private TarifaRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new TarifaRepositoryJdbc(gestor);
    }

    @Test
    @DisplayName("Devuelve la tarifa vigente de CARRO (1800) con su tipo y sin fecha de fin")
    void tarifaVigenteDeCarro() {
        enTransaccionRevertida(() -> {
            TipoVehiculo carro = tipo("CARRO");

            assertThat(repositorio.buscarVigente(carro.getId(), hora(10, 0))).hasValueSatisfying(tarifa -> {
                assertThat(tarifa.getId()).isEqualTo(tarifaVigente("CARRO").getId());
                assertThat(tarifa.getValorHora()).isEqualByComparingTo("1800");
                assertThat(tarifa.getTipoVehiculo().getCodigo()).isEqualTo("CARRO");
                assertThat(tarifa.getVigenteDesde()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
                assertThat(tarifa.getVigenteHasta()).isEmpty();
            });
        });
    }

    @Test
    @DisplayName("Antes del inicio de la primera tarifa no hay tarifa vigente")
    void antesDelInicioDevuelveVacio() {
        enTransaccionRevertida(() -> {
            TipoVehiculo carro = tipo("CARRO");

            assertThat(repositorio.buscarVigente(carro.getId(), LocalDateTime.of(2025, 12, 31, 23, 59))).isEmpty();
        });
    }

    @Test
    @DisplayName("Con una tarifa cerrada y otra nueva, cada fecha obtiene la suya; la fecha de fin ya es de la nueva")
    void tarifaCerradaYNueva() {
        enTransaccionRevertida(() -> {
            TipoVehiculo carro = tipo("CARRO");
            Tarifa anterior = tarifaVigente("CARRO");
            LocalDateTime cambio = hora(12, 0);
            fila("UPDATE tarifa SET vigente_hasta = ? WHERE id = ? RETURNING id", cambio, anterior.getId());
            fila("INSERT INTO tarifa (tipo_vehiculo_id, valor_hora, vigente_desde) VALUES (?, ?, ?) RETURNING id",
                    carro.getId(), new BigDecimal("2500"), cambio);

            Tarifa antesDelCambio = repositorio.buscarVigente(carro.getId(), hora(11, 59)).orElseThrow();
            Tarifa enElCambio = repositorio.buscarVigente(carro.getId(), cambio).orElseThrow();
            Tarifa despues = repositorio.buscarVigente(carro.getId(), hora(18, 0)).orElseThrow();

            assertThat(antesDelCambio.getValorHora()).isEqualByComparingTo("1800");
            assertThat(antesDelCambio.getVigenteHasta()).contains(cambio);
            assertThat(enElCambio.getValorHora()).isEqualByComparingTo("2500");
            assertThat(despues.getValorHora()).isEqualByComparingTo("2500");
            assertThat(despues.getVigenteHasta()).isEmpty();
        });
    }
}
