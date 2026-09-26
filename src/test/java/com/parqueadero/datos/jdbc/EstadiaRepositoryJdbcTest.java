package com.parqueadero.datos.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.datos.repositorio.EstadiaRepository;
import com.parqueadero.dominio.cobro.CobroPorHoraIniciada;
import com.parqueadero.dominio.cobro.PoliticaCobro;
import com.parqueadero.dominio.modelo.CanalPago;
import com.parqueadero.dominio.modelo.EstadoEstadia;
import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.dominio.modelo.Vehiculo;

class EstadiaRepositoryJdbcTest extends IntegracionBD {

    private static final LocalDateTime INGRESO = hora(8, 0);
    private static final LocalDateTime SALIDA = hora(10, 40);   // 160 min → $5.400
    private static final LocalDateTime PAGO = hora(10, 45);

    private final PoliticaCobro cobro = new CobroPorHoraIniciada();
    private EstadiaRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new EstadiaRepositoryJdbc(gestor);
    }

    @Test
    @DisplayName("guardar asigna el id generado y la estadía se recupera completa por placa")
    void guardarYRecuperar() {
        enTransaccionRevertida(() -> {
            Vehiculo vehiculo = insertarVehiculo("ZZT901", "CARRO");

            Estadia guardada = repositorio.guardar(Estadia.iniciar(vehiculo, tarifaVigente("CARRO"), INGRESO));

            assertThat(guardada.getId()).isNotNull();
            assertThat(repositorio.buscarActivaPorPlaca("ZZT901")).hasValueSatisfying(leida -> {
                assertThat(leida.getId()).isEqualTo(guardada.getId());
                assertThat(leida.getEstado()).isEqualTo(EstadoEstadia.DENTRO);
                assertThat(leida.getFechaIngreso()).isEqualTo(INGRESO);
                assertThat(leida.getVehiculo().getPlaca()).isEqualTo("ZZT901");
                assertThat(leida.getVehiculo().getTipoVehiculo().getCodigo()).isEqualTo("CARRO");
                assertThat(leida.getTarifa().getValorHora()).isEqualByComparingTo("1800");
                assertThat(leida.getFechaSalida()).isEmpty();
                assertThat(leida.getValorTotal()).isEmpty();
                assertThat(leida.getPago()).isEmpty();
            });
        });
    }

    @Test
    @DisplayName("Sin estadía activa para la placa devuelve vacío")
    void sinEstadiaActiva() {
        enTransaccionRevertida(() ->
            assertThat(repositorio.buscarActivaPorPlaca("NOEXISTE1")).isEmpty());
    }

    @Test
    @DisplayName("actualizar guarda la salida: PENDIENTE_PAGO, hora de salida y valor total")
    void actualizarGuardaLaSalida() {
        enTransaccionRevertida(() -> {
            Estadia estadia = estadiaGuardada("ZZT901");

            estadia.registrarSalida(SALIDA, cobro);
            repositorio.actualizar(estadia);

            assertThat(repositorio.buscarActivaPorPlaca("ZZT901")).hasValueSatisfying(leida -> {
                assertThat(leida.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);
                assertThat(leida.getFechaSalida()).contains(SALIDA);
                assertThat(leida.getValorTotal()).hasValueSatisfying(
                        valor -> assertThat(valor).isEqualByComparingTo("5400"));
            });
        });
    }

    @Test
    @DisplayName("actualizar guarda el pago en caja con su empleado, y la estadía deja de estar activa")
    void actualizarGuardaElPagoEnCaja() {
        enTransaccionRevertida(() -> {
            Estadia estadia = estadiaGuardada("ZZT901");
            estadia.registrarSalida(SALIDA, cobro);
            repositorio.actualizar(estadia);

            estadia.pagarEnCaja(PAGO, usuario("personal"));
            repositorio.actualizar(estadia);

            assertThat(repositorio.buscarActivaPorPlaca("ZZT901")).isEmpty();

            Map<String, Object> estadiaBD = fila("SELECT estado FROM estadia WHERE id = ?", estadia.getId());
            assertThat(estadiaBD.get("estado")).isEqualTo("CERRADA");

            Map<String, Object> pago = fila("SELECT valor, canal, usuario_id, referencia FROM pago WHERE estadia_id = ?",
                    estadia.getId());
            assertThat(pago).isNotNull();
            assertThat(pago.get("canal")).isEqualTo(CanalPago.CAJA.name());
            assertThat(pago.get("usuario_id")).isEqualTo(usuario("personal").getId());
            assertThat(pago.get("referencia")).isNull();
            assertThat((java.math.BigDecimal) pago.get("valor")).isEqualByComparingTo("5400");
        });
    }

    @Test
    @DisplayName("actualizar guarda el pago en línea con su referencia y sin empleado")
    void actualizarGuardaElPagoEnLinea() {
        enTransaccionRevertida(() -> {
            Estadia estadia = estadiaGuardada("ZZT901");
            estadia.registrarSalida(SALIDA, cobro);
            repositorio.actualizar(estadia);

            estadia.pagarEnLinea(PAGO, "TX-PRUEBA-1");
            repositorio.actualizar(estadia);

            Map<String, Object> pago = fila("SELECT canal, usuario_id, referencia FROM pago WHERE estadia_id = ?",
                    estadia.getId());
            assertThat(pago.get("canal")).isEqualTo(CanalPago.EN_LINEA.name());
            assertThat(pago.get("usuario_id")).isNull();
            assertThat(pago.get("referencia")).isEqualTo("TX-PRUEBA-1");
        });
    }

    @Test
    @DisplayName("CU-07: listarActivas incluye DENTRO y PENDIENTE_PAGO, pero no CERRADA")
    void listarActivas() {
        enTransaccionRevertida(() -> {
            estadiaGuardada("ZZT901");                                   // DENTRO

            Estadia pendiente = estadiaGuardada("ZZT902");               // PENDIENTE_PAGO
            pendiente.registrarSalida(SALIDA, cobro);
            repositorio.actualizar(pendiente);

            Estadia cerrada = estadiaGuardada("ZZT903");                 // CERRADA
            cerrada.registrarSalida(SALIDA, cobro);
            repositorio.actualizar(cerrada);
            cerrada.pagarEnCaja(PAGO, usuario("personal"));
            repositorio.actualizar(cerrada);

            List<Estadia> activas = repositorio.listarActivas();

            assertThat(activas).extracting(e -> e.getVehiculo().getPlaca())
                    .contains("ZZT901", "ZZT902")
                    .doesNotContain("ZZT903");
            assertThat(activas).filteredOn(e -> e.getVehiculo().getPlaca().equals("ZZT902"))
                    .singleElement()
                    .satisfies(e -> assertThat(e.getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO));
        });
    }

    @Test
    @DisplayName("RN-08: la base de datos impide dos estadías activas del mismo vehículo")
    void laBaseImpideDosEstadiasActivas() {
        enTransaccionRevertida(() -> {
            Vehiculo vehiculo = insertarVehiculo("ZZT901", "CARRO");
            repositorio.guardar(Estadia.iniciar(vehiculo, tarifaVigente("CARRO"), INGRESO));

            Estadia segunda = Estadia.iniciar(vehiculo, tarifaVigente("CARRO"), INGRESO.plusHours(1));

            assertThatThrownBy(() -> repositorio.guardar(segunda))
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Estadia estadiaGuardada(String placa) {
        Vehiculo vehiculo = insertarVehiculo(placa, "CARRO");
        return repositorio.guardar(Estadia.iniciar(vehiculo, tarifaVigente("CARRO"), INGRESO));
    }
}
