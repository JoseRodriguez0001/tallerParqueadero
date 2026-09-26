package com.parqueadero.dominio.cobro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;

class CobroPorHoraIniciadaTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 25, 8, 0);

    private final PoliticaCobro politica = new CobroPorHoraIniciada();
    private final Tarifa tarifaCarro = tarifa("CARRO", "Automóvil", "1800");
    private final Tarifa tarifaMoto = tarifa("MOTO", "Motocicleta", "800");

    @Test
    @DisplayName("RN-02: ingreso y salida en el mismo minuto cobra el mínimo de una hora")
    void mismoMinutoCobraUnaHora() {
        // Preparar
        LocalDateTime salida = INGRESO;

        // Ejecutar
        BigDecimal valor = politica.calcular(tarifaCarro, INGRESO, salida);

        // Verificar: isEqualByComparingTo compara el número, no la escala (1800 == 1800.00)
        assertThat(valor).isEqualByComparingTo("1800");
    }

    @Test
    @DisplayName("RN-02: un minuto de permanencia cobra la hora iniciada completa")
    void unMinutoCobraUnaHora() {
        BigDecimal valor = politica.calcular(tarifaCarro, INGRESO, INGRESO.plusMinutes(1));

        assertThat(valor).isEqualByComparingTo("1800");
    }

    @Test
    @DisplayName("RN-17: 60 minutos exactos cobran 1 hora")
    void sesentaMinutosCobraUnaHora() {
        BigDecimal valor = politica.calcular(tarifaCarro, INGRESO, INGRESO.plusMinutes(60));

        assertThat(valor).isEqualByComparingTo("1800");
    }

    @Test
    @DisplayName("RN-17: los segundos se descartan (60 min 59 s cobra 1 hora)")
    void losSegundosSeDescartan() {
        BigDecimal valor = politica.calcular(tarifaCarro, INGRESO, INGRESO.plusMinutes(60).plusSeconds(59));

        assertThat(valor).isEqualByComparingTo("1800");
    }

    @Test
    @DisplayName("RN-17: 61 minutos cobran 2 horas")
    void sesentaYUnMinutosCobraDosHoras() {
        BigDecimal valor = politica.calcular(tarifaCarro, INGRESO, INGRESO.plusMinutes(61));

        assertThat(valor).isEqualByComparingTo("3600");
    }

    @Test
    @DisplayName("Ejemplo del documento (6.2): automóvil de 8:00 a 10:40 paga $5.400")
    void ejemploDelDocumento() {
        LocalDateTime salida = LocalDateTime.of(2026, 9, 25, 10, 40);

        BigDecimal valor = politica.calcular(tarifaCarro, INGRESO, salida);

        assertThat(valor).isEqualByComparingTo("5400");
    }

    @Test
    @DisplayName("RN-03: el valor sale de la tarifa, no está fijo en el código (moto, 160 min = $2.400)")
    void usaElValorDeLaTarifa() {
        BigDecimal valor = politica.calcular(tarifaMoto, INGRESO, INGRESO.plusMinutes(160));

        assertThat(valor).isEqualByComparingTo("2400");
    }

    @Test
    @DisplayName("Una salida anterior al ingreso es un error de programación")
    void salidaAnteriorAlIngresoEsInvalida() {
        assertThatThrownBy(() -> politica.calcular(tarifaCarro, INGRESO, INGRESO.minusMinutes(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static Tarifa tarifa(String codigo, String nombre, String valorHora) {
        TipoVehiculo tipo = TipoVehiculo.reconstruir(1, codigo, nombre, true);
        return Tarifa.reconstruir(1, tipo, new BigDecimal(valorHora), INGRESO.minusYears(1), null);
    }
}
