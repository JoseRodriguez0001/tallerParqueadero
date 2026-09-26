package com.parqueadero.negocio.servicio.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.datos.repositorio.VehiculoRepository;
import com.parqueadero.datos.transaccion.GestorTransacciones;
import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Vehiculo;
import com.parqueadero.negocio.dto.VehiculoDTO;

class VehiculoServiceImplTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 26, 8, 0);

    private final TipoVehiculo carro = TipoVehiculo.reconstruir(2, "CARRO", "Automóvil", true);
    private final TipoVehiculo moto = TipoVehiculo.reconstruir(1, "MOTO", "Motocicleta", true);

    private GestorFalso gestor;
    private VehiculoRepository vehiculos;
    private TipoVehiculoRepository tipos;
    private EstadiaRepository estadias;
    private VehiculoServiceImpl servicio;

    @BeforeEach
    void setUp() {
        gestor = new GestorFalso();
        vehiculos = mock(VehiculoRepository.class);
        tipos = mock(TipoVehiculoRepository.class);
        estadias = mock(EstadiaRepository.class);
        servicio = new VehiculoServiceImpl(gestor, vehiculos, tipos, estadias);

        // Por defecto: ninguna placa registrada, y guardar asigna un id como lo haría la BD
        when(vehiculos.buscarPorPlaca(any())).thenReturn(Optional.empty());
        when(vehiculos.guardar(any())).thenAnswer(invocacion -> {
            Vehiculo nuevo = invocacion.getArgument(0);
            return Vehiculo.reconstruir(50, nuevo.getPlaca(), nuevo.getTipoVehiculo());
        });
        when(tipos.buscarPorCodigo("CARRO")).thenReturn(Optional.of(carro));
    }

    // ── CU-04 Registrar vehículo ───────────────────────────────────────────────

    @Nested
    @DisplayName("registrar (CU-04)")
    class Registrar {

        @Test
        @DisplayName("Normaliza la placa, guarda el vehículo y lo devuelve fuera del parqueadero, en una transacción")
        void registroValido() {
            VehiculoDTO dto = servicio.registrar(" abc-123 ", "CARRO");

            ArgumentCaptor<Vehiculo> guardado = ArgumentCaptor.forClass(Vehiculo.class);
            verify(vehiculos).guardar(guardado.capture());
            assertThat(guardado.getValue().getPlaca()).isEqualTo("ABC123");
            assertThat(guardado.getValue().getTipoVehiculo()).isSameAs(carro);
            verify(vehiculos).buscarPorPlaca("ABC123");

            assertThat(dto).isEqualTo(new VehiculoDTO("ABC123", "Automóvil", false));
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("Una placa ya registrada se rechaza y no se guarda nada")
        void placaDuplicada() {
            when(vehiculos.buscarPorPlaca("ABC123")).thenReturn(Optional.of(Vehiculo.reconstruir(10, "ABC123", carro)));

            assertThatThrownBy(() -> servicio.registrar("abc123", "CARRO"))
                    .isInstanceOf(NegocioException.class)
                    .hasMessage("La placa ABC123 ya está registrada.");
            verify(vehiculos, never()).guardar(any());
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("Un código de tipo que no existe se rechaza")
        void tipoInexistente() {
            when(tipos.buscarPorCodigo("AVION")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.registrar("ABC123", "AVION"))
                    .isInstanceOf(NegocioException.class)
                    .hasMessage("El tipo de vehículo AVION no existe.");
            verify(vehiculos, never()).guardar(any());
        }

        @Test
        @DisplayName("Un tipo inactivo se rechaza por la regla del dominio")
        void tipoInactivo() {
            when(tipos.buscarPorCodigo("BICI"))
                    .thenReturn(Optional.of(TipoVehiculo.reconstruir(3, "BICI", "Bicicleta", false)));

            assertThatThrownBy(() -> servicio.registrar("ABC123", "BICI"))
                    .isInstanceOf(NegocioException.class)
                    .hasMessage("El tipo de vehículo Bicicleta no está disponible.");
            verify(vehiculos, never()).guardar(any());
        }

        @Test
        @DisplayName("Una placa vacía se rechaza por la regla del dominio")
        void placaVacia() {
            assertThatThrownBy(() -> servicio.registrar("  ", "CARRO"))
                    .isInstanceOf(NegocioException.class)
                    .hasMessage("Ingrese la placa del vehículo.");
            verify(vehiculos, never()).guardar(any());
        }
    }

    // ── CU-07 Registrados ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarRegistrados (CU-07)")
    class ListarRegistrados {

        @Test
        @DisplayName("Marca como dentro solo los vehículos con estadía activa")
        void marcaLosQueEstanDentro() {
            Vehiculo abc = Vehiculo.reconstruir(10, "ABC123", carro);
            Vehiculo def = Vehiculo.reconstruir(11, "DEF456", moto);
            Tarifa tarifaCarro = Tarifa.reconstruir(20, carro, new BigDecimal("1800"), INGRESO.minusYears(1), null);
            when(estadias.listarActivas()).thenReturn(List.of(Estadia.iniciar(abc, tarifaCarro, INGRESO)));
            when(vehiculos.listarTodos()).thenReturn(List.of(abc, def));

            List<VehiculoDTO> lista = servicio.listarRegistrados();

            assertThat(lista).containsExactly(
                    new VehiculoDTO("ABC123", "Automóvil", true),
                    new VehiculoDTO("DEF456", "Motocicleta", false));
            assertThat(gestor.transacciones).isEqualTo(1);
        }

        @Test
        @DisplayName("Sin vehículos registrados devuelve una lista vacía")
        void sinVehiculos() {
            when(estadias.listarActivas()).thenReturn(List.of());
            when(vehiculos.listarTodos()).thenReturn(List.of());

            assertThat(servicio.listarRegistrados()).isEmpty();
        }
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
