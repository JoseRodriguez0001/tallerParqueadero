package com.parqueadero.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.dominio.cobro.CobroPorHoraIniciada;
import com.parqueadero.dominio.cobro.PoliticaCobro;

class EstadiaTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 25, 8, 0);
    private static final LocalDateTime SALIDA = LocalDateTime.of(2026, 9, 25, 10, 40);   // 160 min → $5.400
    private static final LocalDateTime PAGO = LocalDateTime.of(2026, 9, 25, 10, 45);
    private static final LocalDateTime INICIO_TARIFA = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final PoliticaCobro cobro = new CobroPorHoraIniciada();

    private final TipoVehiculo carro = TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true);
    private final TipoVehiculo moto = TipoVehiculo.reconstruir(1, "MOTO", "Motocicleta", true);

    private final Vehiculo vehiculoCarro = Vehiculo.reconstruir(10, "ABC123", carro);
    private final Tarifa tarifaCarro = Tarifa.reconstruir(20, carro, new BigDecimal("1800"), INICIO_TARIFA, null);
    private final Usuario empleado = Usuario.reconstruir(1, "personal", "Empleado del parqueadero", "hash", Rol.PERSONAL, true);

    // ── Incremento 2: iniciar y estaActiva ─────────────────────────────────────

    @Nested
    @DisplayName("iniciar (CU-03 Registrar ingreso)")
    class Iniciar {

        @Test
        @DisplayName("RN-07: un ingreso válido crea la estadía en DENTRO, sin salida, valor ni pago")
        void ingresoValido() {
            Estadia estadia = Estadia.iniciar(vehiculoCarro, tarifaCarro, INGRESO);

            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.DENTRO);
            assertThat(estadia.getId()).isNull();
            assertThat(estadia.getVehiculo()).isSameAs(vehiculoCarro);
            assertThat(estadia.getTarifa()).isSameAs(tarifaCarro);
            assertThat(estadia.getFechaIngreso()).isEqualTo(INGRESO);
            assertThat(estadia.getFechaSalida()).isEmpty();
            assertThat(estadia.getValorTotal()).isEmpty();
            assertThat(estadia.getPago()).isEmpty();
        }

        @Test
        @DisplayName("RN-16: no se permite el ingreso de un tipo de vehículo inactivo")
        void tipoInactivo() {
            TipoVehiculo camionetaInactiva = TipoVehiculo.reconstruir(3, "CAMIONETA", "Camioneta", false);
            Vehiculo camioneta = Vehiculo.reconstruir(11, "XYZ789", camionetaInactiva);
            Tarifa tarifaCamioneta = Tarifa.reconstruir(21, camionetaInactiva, new BigDecimal("2500"), INICIO_TARIFA, null);

            assertThatThrownBy(() -> Estadia.iniciar(camioneta, tarifaCamioneta, INGRESO))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("inactivo");
        }

        @Test
        @DisplayName("La tarifa debe corresponder al tipo del vehículo")
        void tarifaDeOtroTipo() {
            Tarifa tarifaMoto = Tarifa.reconstruir(22, moto, new BigDecimal("800"), INICIO_TARIFA, null);

            assertThatThrownBy(() -> Estadia.iniciar(vehiculoCarro, tarifaMoto, INGRESO))
                    .isInstanceOf(NegocioException.class);
        }

        @Test
        @DisplayName("RN-01: la tarifa debe estar vigente en la fecha de ingreso")
        void tarifaVencida() {
            Tarifa tarifaVencida = Tarifa.reconstruir(23, carro, new BigDecimal("1500"),
                    LocalDateTime.of(2025, 1, 1, 0, 0), INICIO_TARIFA);

            assertThatThrownBy(() -> Estadia.iniciar(vehiculoCarro, tarifaVencida, INGRESO))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("vigente");
        }
    }

    @Nested
    @DisplayName("estaActiva (RN-08)")
    class EstaActiva {

        @Test
        @DisplayName("Una estadía DENTRO está activa")
        void dentroEstaActiva() {
            assertThat(estadiaEn(EstadoEstadia.DENTRO).estaActiva()).isTrue();
        }

        @Test
        @DisplayName("Una estadía PENDIENTE_PAGO sigue activa: bloquea un nuevo ingreso")
        void pendienteEstaActiva() {
            assertThat(estadiaEn(EstadoEstadia.PENDIENTE_PAGO).estaActiva()).isTrue();
        }

        @Test
        @DisplayName("Una estadía CERRADA no está activa")
        void cerradaNoEstaActiva() {
            assertThat(estadiaEn(EstadoEstadia.CERRADA).estaActiva()).isFalse();
        }
    }

    // ── Incremento 3: registrarSalida y calcularValor ──────────────────────────

    @Nested
    @DisplayName("registrarSalida (CU-05 Registrar salida)")
    class RegistrarSalida {

        @Test
        @DisplayName("RN-09: fija la hora de salida y el valor total, y pasa a PENDIENTE_PAGO")
        void salidaValida() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);

            estadia.registrarSalida(SALIDA, cobro);

            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);
            assertThat(estadia.getFechaSalida()).contains(SALIDA);
            assertThat(estadia.getValorTotal()).hasValueSatisfying(
                    valor -> assertThat(valor).isEqualByComparingTo("5400"));
            assertThat(estadia.estaActiva()).isTrue();
        }

        @Test
        @DisplayName("Strategy: el valor lo calcula la política recibida, sea cual sea")
        void usaLaPoliticaRecibida() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);
            PoliticaCobro tarifaPlana = (tarifa, ingreso, salida) -> new BigDecimal("999");

            estadia.registrarSalida(SALIDA, tarifaPlana);

            assertThat(estadia.getValorTotal()).hasValueSatisfying(
                    valor -> assertThat(valor).isEqualByComparingTo("999"));
        }

        @Test
        @DisplayName("RN-09: no se puede registrar la salida de una estadía ya liquidada")
        void noSePuedeLiquidarDosVeces() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);

            assertThatThrownBy(() -> estadia.registrarSalida(SALIDA.plusHours(1), cobro))
                    .isInstanceOf(NegocioException.class);
            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);
        }

        @Test
        @DisplayName("RN-09: no se puede registrar la salida de una estadía cerrada")
        void cerradaNoAdmiteSalida() {
            Estadia estadia = estadiaEn(EstadoEstadia.CERRADA);

            assertThatThrownBy(() -> estadia.registrarSalida(SALIDA, cobro))
                    .isInstanceOf(NegocioException.class);
        }

        @Test
        @DisplayName("Si el cálculo falla, la estadía queda exactamente como estaba")
        void siFallaNoQuedaAMedias() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);

            assertThatThrownBy(() -> estadia.registrarSalida(INGRESO.minusMinutes(1), cobro))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.DENTRO);
            assertThat(estadia.getFechaSalida()).isEmpty();
            assertThat(estadia.getValorTotal()).isEmpty();
        }
    }

    @Nested
    @DisplayName("calcularValor (CU-01 Consultar valor, RN-13)")
    class CalcularValor {

        @Test
        @DisplayName("RN-13: estando DENTRO devuelve el estimado a la hora consultada")
        void dentroDevuelveEstimado() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);

            BigDecimal valor = estadia.calcularValor(SALIDA, cobro);

            assertThat(valor).isEqualByComparingTo("5400");
        }

        @Test
        @DisplayName("RN-13: consultar el estimado no modifica la estadía")
        void consultarNoModifica() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);

            estadia.calcularValor(SALIDA, cobro);

            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.DENTRO);
            assertThat(estadia.getFechaSalida()).isEmpty();
            assertThat(estadia.getValorTotal()).isEmpty();
        }

        @Test
        @DisplayName("RN-13: liquidada, devuelve el valor definitivo aunque pase el tiempo")
        void pendienteDevuelveDefinitivo() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);

            BigDecimal valor = estadia.calcularValor(INGRESO.plusHours(10), cobro);

            assertThat(valor).isEqualByComparingTo("3600");
        }

        @Test
        @DisplayName("RN-13: cerrada, devuelve el valor que se pagó")
        void cerradaDevuelveDefinitivo() {
            Estadia estadia = estadiaEn(EstadoEstadia.CERRADA);

            BigDecimal valor = estadia.calcularValor(INGRESO.plusHours(10), cobro);

            assertThat(valor).isEqualByComparingTo("3600");
        }
    }

    // ── Incremento 4: pagarEnCaja y pagarEnLinea ───────────────────────────────

    @Nested
    @DisplayName("pagarEnCaja (CU-06 Registrar pago en caja)")
    class PagarEnCaja {

        @Test
        @DisplayName("RN-10, RN-18: paga el valor total, registra al empleado y pasa a CERRADA")
        void pagoValido() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);

            estadia.pagarEnCaja(PAGO, empleado);

            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.CERRADA);
            assertThat(estadia.estaActiva()).isFalse();
            assertThat(estadia.getPago()).hasValueSatisfying(pago -> {
                assertThat(pago.getValor()).isEqualByComparingTo("3600");
                assertThat(pago.getFechaPago()).isEqualTo(PAGO);
                assertThat(pago.getCanal()).isEqualTo(CanalPago.CAJA);
                assertThat(pago.getEmpleado()).containsSame(empleado);
                assertThat(pago.getReferencia()).isEmpty();
            });
        }

        @Test
        @DisplayName("RN-10: no se puede pagar una estadía que sigue DENTRO")
        void noSePuedePagarDentro() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);

            assertThatThrownBy(() -> estadia.pagarEnCaja(PAGO, empleado))
                    .isInstanceOf(NegocioException.class);
            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.DENTRO);
            assertThat(estadia.getPago()).isEmpty();
        }

        @Test
        @DisplayName("RN-11: no se puede pagar dos veces")
        void noSePuedePagarDosVeces() {
            Estadia estadia = estadiaEn(EstadoEstadia.CERRADA);
            Pago pagoOriginal = estadia.getPago().orElseThrow();

            assertThatThrownBy(() -> estadia.pagarEnCaja(PAGO, empleado))
                    .isInstanceOf(NegocioException.class);
            assertThat(estadia.getPago()).containsSame(pagoOriginal);
        }

        @Test
        @DisplayName("RN-20: un empleado inactivo no puede registrar pagos")
        void empleadoInactivo() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);
            Usuario inactivo = Usuario.reconstruir(2, "exempleado", "Ex empleado", "hash", Rol.PERSONAL, false);

            assertThatThrownBy(() -> estadia.pagarEnCaja(PAGO, inactivo))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("inactivo");
            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);
            assertThat(estadia.getPago()).isEmpty();
        }

        @Test
        @DisplayName("RN-18: sin empleado no hay pago en caja, y la estadía queda como estaba")
        void empleadoObligatorio() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);

            assertThatThrownBy(() -> estadia.pagarEnCaja(PAGO, null))
                    .isInstanceOf(NullPointerException.class);
            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);
            assertThat(estadia.getPago()).isEmpty();
        }
    }

    @Nested
    @DisplayName("pagarEnLinea (CU-02 Pagar en línea)")
    class PagarEnLinea {

        @Test
        @DisplayName("RN-12, RN-18: guarda la referencia de la pasarela, sin empleado, y pasa a CERRADA")
        void pagoValido() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);

            estadia.pagarEnLinea(PAGO, "TX-0001");

            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.CERRADA);
            assertThat(estadia.getPago()).hasValueSatisfying(pago -> {
                assertThat(pago.getValor()).isEqualByComparingTo("3600");
                assertThat(pago.getCanal()).isEqualTo(CanalPago.EN_LINEA);
                assertThat(pago.getReferencia()).contains("TX-0001");
                assertThat(pago.getEmpleado()).isEmpty();
            });
        }

        @Test
        @DisplayName("RN-10: no se puede pagar en línea una estadía que sigue DENTRO")
        void noSePuedePagarDentro() {
            Estadia estadia = estadiaEn(EstadoEstadia.DENTRO);

            assertThatThrownBy(() -> estadia.pagarEnLinea(PAGO, "TX-0001"))
                    .isInstanceOf(NegocioException.class);
            assertThat(estadia.getPago()).isEmpty();
        }

        @Test
        @DisplayName("RN-12: sin referencia de la pasarela no hay pago, y la estadía queda como estaba")
        void referenciaObligatoria() {
            Estadia estadia = estadiaEn(EstadoEstadia.PENDIENTE_PAGO);

            assertThatThrownBy(() -> estadia.pagarEnLinea(PAGO, null))
                    .isInstanceOf(NullPointerException.class);
            assertThat(estadia.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);
            assertThat(estadia.getPago()).isEmpty();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    // Estadía en un estado dado, con datos coherentes con ese estado (CERRADA incluye su pago)
    private Estadia estadiaEn(EstadoEstadia estado) {
        boolean dentro = estado == EstadoEstadia.DENTRO;
        LocalDateTime salida = dentro ? null : INGRESO.plusHours(2);
        BigDecimal valor = dentro ? null : new BigDecimal("3600");
        Pago pago = estado == EstadoEstadia.CERRADA
                ? Pago.reconstruir(30, valor, salida, CanalPago.CAJA, null, empleado)
                : null;
        return Estadia.reconstruir(1, vehiculoCarro, tarifaCarro, INGRESO, salida, valor, estado, pago);
    }
}
