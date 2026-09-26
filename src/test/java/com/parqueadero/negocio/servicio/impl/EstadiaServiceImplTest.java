package com.parqueadero.negocio.servicio.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.datos.repositorio.EstadiaRepository;
import com.parqueadero.datos.repositorio.TarifaRepository;
import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.datos.repositorio.UsuarioRepository;
import com.parqueadero.datos.repositorio.VehiculoRepository;
import com.parqueadero.datos.transaccion.GestorTransacciones;
import com.parqueadero.dominio.cobro.CobroPorHoraIniciada;
import com.parqueadero.dominio.modelo.CanalPago;
import com.parqueadero.dominio.modelo.EstadoEstadia;
import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.dominio.modelo.Rol;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Usuario;
import com.parqueadero.dominio.modelo.Vehiculo;
import com.parqueadero.negocio.dto.EstadiaDTO;

class EstadiaServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 26, 8, 0);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 26, 10, 40);   // 160 min después
    private static final String USUARIO_CAJA = "personal";

    private final TipoVehiculo carro = TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true);
    private final TipoVehiculo moto = TipoVehiculo.reconstruir(1, "MOTO", "Motocicleta", true);
    private final Tarifa tarifaCarro = Tarifa.reconstruir(20, carro, new BigDecimal("1800"), INGRESO.minusYears(1), null);
    private final Tarifa tarifaMoto = Tarifa.reconstruir(21, moto, new BigDecimal("800"), INGRESO.minusYears(1), null);
    private final Vehiculo vehiculoCarro = Vehiculo.reconstruir(10, "ABC123", carro);
    private final Usuario empleado = Usuario.reconstruir(1, USUARIO_CAJA, "Empleado", "hash", Rol.PERSONAL, true);

    private GestorFalso gestor;
    private EstadiaRepository estadias;
    private VehiculoRepository vehiculos;
    private TipoVehiculoRepository tipos;
    private TarifaRepository tarifas;
    private UsuarioRepository usuarios;
    private EstadiaServiceImpl servicio;

    @BeforeEach
    void setUp() {
        gestor = new GestorFalso();
        estadias = mock(EstadiaRepository.class);
        vehiculos = mock(VehiculoRepository.class);
        tipos = mock(TipoVehiculoRepository.class);
        tarifas = mock(TarifaRepository.class);
        usuarios = mock(UsuarioRepository.class);

        Clock relojFijo = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        servicio = new EstadiaServiceImpl(gestor, estadias, vehiculos, tipos, tarifas, usuarios,
                new CobroPorHoraIniciada(), USUARIO_CAJA, relojFijo);

        // Por defecto: nadie tiene estadía activa, y guardar asigna un id como lo haría la BD
        when(estadias.buscarActivaPorPlaca(any())).thenReturn(Optional.empty());
        when(estadias.guardar(any())).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));
        when(vehiculos.guardar(any())).thenAnswer(invocacion -> {
            Vehiculo nuevo = invocacion.getArgument(0);
            return Vehiculo.reconstruir(50, nuevo.getPlaca(), nuevo.getTipoVehiculo());
        });
        when(tarifas.buscarVigente(carro.getId(), AHORA)).thenReturn(Optional.of(tarifaCarro));
        when(tarifas.buscarVigente(moto.getId(), AHORA)).thenReturn(Optional.of(tarifaMoto));
        when(usuarios.buscarPorNombreUsuario(USUARIO_CAJA)).thenReturn(Optional.of(empleado));
    }

    // ── CU-03 Registrar ingreso ────────────────────────────────────────────────

    @Nested
    @DisplayName("registrarIngreso (CU-03, CU-04 extend)")
    class RegistrarIngreso {

        @Test
        @DisplayName("Vehículo registrado: crea la estadía DENTRO a la hora del servidor, en una transacción")
        void vehiculoRegistrado() {
            when(vehiculos.buscarPorPlaca("ABC123")).thenReturn(Optional.of(vehiculoCarro));

            EstadiaDTO dto = servicio.registrarIngreso("ABC123", null);

            assertThat(dto.placa()).isEqualTo("ABC123");
            assertThat(dto.tipo()).isEqualTo("Automóvil");
            assertThat(dto.estado()).isEqualTo("DENTRO");
            assertThat(dto.fechaIngreso()).isEqualTo(AHORA);
            assertThat(dto.fechaSalida()).isNull();
            assertThat(dto.valor()).isEqualByComparingTo("1800");   // estimado: 0 min → mínimo 1 hora
            verify(estadias).guardar(any());
            verify(vehiculos, never()).guardar(any());
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("RN-05: la placa se normaliza antes de buscar (' abc-123 ' → 'ABC123')")
        void normalizaLaPlaca() {
            when(vehiculos.buscarPorPlaca("ABC123")).thenReturn(Optional.of(vehiculoCarro));

            servicio.registrarIngreso(" abc-123 ", null);

            verify(vehiculos).buscarPorPlaca("ABC123");
        }

        @Test
        @DisplayName("RN-06: placa nueva con tipo → registra el vehículo y su estadía en la misma transacción")
        void placaNuevaRegistraElVehiculo() {
            when(vehiculos.buscarPorPlaca("XYZ98D")).thenReturn(Optional.empty());
            when(tipos.buscarPorCodigo("MOTO")).thenReturn(Optional.of(moto));

            EstadiaDTO dto = servicio.registrarIngreso("XYZ98D", "MOTO");

            ArgumentCaptor<Vehiculo> vehiculoGuardado = ArgumentCaptor.forClass(Vehiculo.class);
            verify(vehiculos).guardar(vehiculoGuardado.capture());
            assertThat(vehiculoGuardado.getValue().getPlaca()).isEqualTo("XYZ98D");
            assertThat(vehiculoGuardado.getValue().getTipoVehiculo().getCodigo()).isEqualTo("MOTO");

            assertThat(dto.tipo()).isEqualTo("Motocicleta");
            assertThat(dto.valor()).isEqualByComparingTo("800");
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("RN-06: placa nueva sin tipo → pide el tipo y no guarda nada")
        void placaNuevaSinTipo() {
            when(vehiculos.buscarPorPlaca("XYZ98D")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.registrarIngreso("XYZ98D", null))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("tipo");
            verify(vehiculos, never()).guardar(any());
            verify(estadias, never()).guardar(any());
        }

        @Test
        @DisplayName("Un código de tipo que no existe se rechaza")
        void tipoInexistente() {
            when(vehiculos.buscarPorPlaca("XYZ98D")).thenReturn(Optional.empty());
            when(tipos.buscarPorCodigo("AVION")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.registrarIngreso("XYZ98D", "AVION"))
                    .isInstanceOf(NegocioException.class);
            verify(vehiculos, never()).guardar(any());
        }

        @Test
        @DisplayName("RN-08: un vehículo con estadía activa no puede volver a ingresar")
        void yaTieneEstadiaActiva() {
            when(vehiculos.buscarPorPlaca("ABC123")).thenReturn(Optional.of(vehiculoCarro));
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaDentro()));

            assertThatThrownBy(() -> servicio.registrarIngreso("ABC123", null))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("ABC123");
            verify(estadias, never()).guardar(any());
        }

        @Test
        @DisplayName("RN-15: sin tarifa vigente para el tipo, no hay ingreso")
        void sinTarifaVigente() {
            when(vehiculos.buscarPorPlaca("ABC123")).thenReturn(Optional.of(vehiculoCarro));
            when(tarifas.buscarVigente(anyInt(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.registrarIngreso("ABC123", null))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("tarifa");
            verify(estadias, never()).guardar(any());
        }
    }

    // ── CU-01 Consultar valor ──────────────────────────────────────────────────

    @Nested
    @DisplayName("consultarValor (CU-01)")
    class ConsultarValor {

        @Test
        @DisplayName("RN-13: estando DENTRO devuelve el estimado a la hora actual y no guarda nada")
        void dentroDevuelveEstimado() {
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaDentro()));

            EstadiaDTO dto = servicio.consultarValor("ABC123");

            assertThat(dto.valor()).isEqualByComparingTo("5400");
            assertThat(dto.estado()).isEqualTo("DENTRO");
            verify(estadias, never()).actualizar(any());
        }

        @Test
        @DisplayName("Sin estadía activa informa que no hay nada que consultar")
        void sinEstadiaActiva() {
            assertThatThrownBy(() -> servicio.consultarValor("ABC123"))
                    .isInstanceOf(NegocioException.class)
                    .hasMessageContaining("ABC123");
        }
    }

    // ── CU-05 Registrar salida ─────────────────────────────────────────────────

    @Nested
    @DisplayName("registrarSalida (CU-05)")
    class RegistrarSalida {

        @Test
        @DisplayName("RN-09: liquida con la hora del servidor, guarda y devuelve PENDIENTE_PAGO")
        void salidaValida() {
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaDentro()));

            EstadiaDTO dto = servicio.registrarSalida("abc123");

            ArgumentCaptor<Estadia> guardada = ArgumentCaptor.forClass(Estadia.class);
            verify(estadias).actualizar(guardada.capture());
            assertThat(guardada.getValue().getEstado()).isEqualTo(EstadoEstadia.PENDIENTE_PAGO);

            assertThat(dto.estado()).isEqualTo("PENDIENTE_PAGO");
            assertThat(dto.fechaSalida()).isEqualTo(AHORA);
            assertThat(dto.valor()).isEqualByComparingTo("5400");
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("Sin estadía activa no hay salida que registrar")
        void sinEstadiaActiva() {
            assertThatThrownBy(() -> servicio.registrarSalida("ABC123"))
                    .isInstanceOf(NegocioException.class);
            verify(estadias, never()).actualizar(any());
        }

        @Test
        @DisplayName("Una estadía ya liquidada no se vuelve a liquidar (regla del dominio)")
        void yaLiquidada() {
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaPendiente()));

            assertThatThrownBy(() -> servicio.registrarSalida("ABC123"))
                    .isInstanceOf(NegocioException.class);
            verify(estadias, never()).actualizar(any());
        }
    }

    // ── CU-06 Registrar pago en caja ───────────────────────────────────────────

    @Nested
    @DisplayName("registrarPagoEnCaja (CU-06)")
    class RegistrarPagoEnCaja {

        @Test
        @DisplayName("RN-18: registra el pago a nombre del usuario de caja configurado y cierra la estadía")
        void pagoValido() {
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaPendiente()));

            EstadiaDTO dto = servicio.registrarPagoEnCaja("ABC123");

            ArgumentCaptor<Estadia> guardada = ArgumentCaptor.forClass(Estadia.class);
            verify(estadias).actualizar(guardada.capture());
            assertThat(guardada.getValue().getPago()).hasValueSatisfying(pago -> {
                assertThat(pago.getCanal()).isEqualTo(CanalPago.CAJA);
                assertThat(pago.getEmpleado()).containsSame(empleado);
                assertThat(pago.getFechaPago()).isEqualTo(AHORA);
            });

            assertThat(dto.estado()).isEqualTo("CERRADA");
            assertThat(dto.valor()).isEqualByComparingTo("5400");
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("RN-10: no se paga una estadía que sigue DENTRO (regla del dominio)")
        void noSePagaDentro() {
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaDentro()));

            assertThatThrownBy(() -> servicio.registrarPagoEnCaja("ABC123"))
                    .isInstanceOf(NegocioException.class);
            verify(estadias, never()).actualizar(any());
        }

        @Test
        @DisplayName("Si el usuario de caja configurado no existe, es un error de configuración")
        void usuarioDeCajaInexistente() {
            when(estadias.buscarActivaPorPlaca("ABC123")).thenReturn(Optional.of(estadiaPendiente()));
            when(usuarios.buscarPorNombreUsuario(USUARIO_CAJA)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.registrarPagoEnCaja("ABC123"))
                    .isInstanceOf(IllegalStateException.class);
            verify(estadias, never()).actualizar(any());
        }
    }

    // ── CU-07 En el parqueadero ────────────────────────────────────────────────

    @Nested
    @DisplayName("listarEnParqueadero (CU-07)")
    class ListarEnParqueadero {

        @Test
        @DisplayName("Convierte cada estadía activa con su valor: estimado si DENTRO, definitivo si liquidada")
        void listaConValores() {
            Estadia dentroDesdeLas10 = Estadia.reconstruir(2, Vehiculo.reconstruir(11, "DEF456", carro), tarifaCarro,
                    LocalDateTime.of(2026, 9, 26, 10, 0), null, null, EstadoEstadia.DENTRO, null);
            when(estadias.listarActivas()).thenReturn(List.of(dentroDesdeLas10, estadiaPendiente()));

            List<EstadiaDTO> lista = servicio.listarEnParqueadero();

            assertThat(lista).hasSize(2);
            assertThat(lista.get(0).placa()).isEqualTo("DEF456");
            assertThat(lista.get(0).valor()).isEqualByComparingTo("1800");   // 40 min → 1 hora
            assertThat(lista.get(1).valor()).isEqualByComparingTo("5400");   // definitivo
            assertThat(gestor.transacciones).isEqualTo(1);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Estadia estadiaDentro() {
        return Estadia.reconstruir(1, vehiculoCarro, tarifaCarro, INGRESO, null, null, EstadoEstadia.DENTRO, null);
    }

    private Estadia estadiaPendiente() {
        return Estadia.reconstruir(1, vehiculoCarro, tarifaCarro, INGRESO, AHORA.minusMinutes(5),
                new BigDecimal("5400"), EstadoEstadia.PENDIENTE_PAGO, null);
    }

    private static Estadia conId(Estadia estadia) {
        return Estadia.reconstruir(100, estadia.getVehiculo(), estadia.getTarifa(), estadia.getFechaIngreso(),
                estadia.getFechaSalida().orElse(null), estadia.getValorTotal().orElse(null),
                estadia.getEstado(), estadia.getPago().orElse(null));
    }

    // Ejecuta la operación directamente y cuenta cuántas transacciones se abrieron
    private static final class GestorFalso implements GestorTransacciones {
        int transacciones;

        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            transacciones++;
            return operacion.get();
        }
    }
}
