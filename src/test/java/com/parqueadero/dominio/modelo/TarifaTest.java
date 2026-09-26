package com.parqueadero.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TarifaTest {

    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime HASTA = LocalDateTime.of(2027, 1, 1, 0, 0);

    private final TipoVehiculo carro = TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true);

    @Test
    @DisplayName("RN-04: una tarifa abierta está vigente desde su inicio en adelante")
    void tarifaAbiertaVigenteDesdeSuInicio() {
        Tarifa abierta = Tarifa.reconstruir(1, carro, new BigDecimal("1800"), DESDE, null);

        assertThat(abierta.estaVigenteEn(DESDE)).isTrue();
        assertThat(abierta.estaVigenteEn(DESDE.plusYears(5))).isTrue();
    }

    @Test
    @DisplayName("RN-04: antes de su fecha de inicio la tarifa no está vigente")
    void antesDelInicioNoEstaVigente() {
        Tarifa abierta = Tarifa.reconstruir(1, carro, new BigDecimal("1800"), DESDE, null);

        assertThat(abierta.estaVigenteEn(DESDE.minusMinutes(1))).isFalse();
    }

    @Test
    @DisplayName("RN-04: la fecha de fin no está incluida (intervalo semiabierto)")
    void laFechaDeFinNoEstaIncluida() {
        Tarifa cerrada = Tarifa.reconstruir(1, carro, new BigDecimal("1800"), DESDE, HASTA);

        assertThat(cerrada.estaVigenteEn(HASTA.minusMinutes(1))).isTrue();
        assertThat(cerrada.estaVigenteEn(HASTA)).isFalse();
    }
}
