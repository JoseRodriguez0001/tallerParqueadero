package com.parqueadero.presentacion.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.negocio.servicio.EstadiaService;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelPagoCaja;
import com.parqueadero.presentacion.vista.PanelSalida;
import com.parqueadero.presentacion.vista.PanelVehiculos;

// Los paneles y el servicio son simulados: se prueba que cada botón llame al servicio correcto
// y que el resultado o el error lleguen al panel, sin abrir ventanas
class EstadiaControllerTest {

    private static final LocalDateTime INGRESO = LocalDateTime.of(2026, 9, 26, 8, 0);

    private final EstadiaDTO dentro = new EstadiaDTO("ABC123", "Automóvil", INGRESO, null, new BigDecimal("1800"), "DENTRO");
    private final EstadiaDTO pendiente = new EstadiaDTO("ABC123", "Automóvil", INGRESO, INGRESO.plusMinutes(160),
            new BigDecimal("5400"), "PENDIENTE_PAGO");
    private final List<EstadiaDTO> enParqueadero = List.of(dentro);

    private EstadiaService servicio;
    private PanelIngreso panelIngreso;
    private PanelSalida panelSalida;
    private PanelPagoCaja panelPagoCaja;
    private PanelVehiculos panelVehiculos;
    private EstadiaController controlador;

    @BeforeEach
    void setUp() {
        servicio = mock(EstadiaService.class);
        panelIngreso = mock(PanelIngreso.class);
        panelSalida = mock(PanelSalida.class);
        panelPagoCaja = mock(PanelPagoCaja.class);
        panelVehiculos = mock(PanelVehiculos.class);

        when(servicio.listarEnParqueadero()).thenReturn(enParqueadero);

        controlador = new EstadiaController(servicio, panelIngreso, panelSalida, panelPagoCaja, panelVehiculos);
    }

    @Test
    @DisplayName("Al iniciar muestra los vehículos que están en el parqueadero")
    void iniciarCargaLaLista() {
        controlador.iniciar();

        verify(panelVehiculos).mostrarEnParqueadero(enParqueadero);
    }

    @Test
    @DisplayName("Registrar ingreso: llama al servicio con placa y tipo, muestra el resultado, limpia y refresca la lista")
    void registrarIngreso() {
        when(panelIngreso.getPlaca()).thenReturn("abc123");
        when(panelIngreso.getCodigoTipo()).thenReturn(null);
        when(servicio.registrarIngreso("abc123", null)).thenReturn(dentro);

        presionar(boton -> verify(panelIngreso).alRegistrar(boton.capture()));

        verify(panelIngreso).mostrarIngreso(dentro);
        verify(panelIngreso).limpiar();
        verify(panelVehiculos).mostrarEnParqueadero(enParqueadero);
    }

    @Test
    @DisplayName("Si el servicio rechaza el ingreso, se muestra su mensaje y no se limpia el formulario")
    void ingresoRechazado() {
        when(panelIngreso.getPlaca()).thenReturn("ABC123");
        when(servicio.registrarIngreso(anyString(), any()))
                .thenThrow(new NegocioException("El vehículo ABC123 ya tiene una estadía activa."));

        presionar(boton -> verify(panelIngreso).alRegistrar(boton.capture()));

        verify(panelIngreso).mostrarError("El vehículo ABC123 ya tiene una estadía activa.");
        verify(panelIngreso, never()).mostrarIngreso(any());
        verify(panelIngreso, never()).limpiar();
    }

    @Test
    @DisplayName("Un error técnico muestra un mensaje genérico, nunca el detalle interno")
    void errorInesperado() {
        when(panelIngreso.getPlaca()).thenReturn("ABC123");
        when(servicio.registrarIngreso(anyString(), any()))
                .thenThrow(new IllegalStateException("Error SQL: relation \"estadia\" does not exist"));

        presionar(boton -> verify(panelIngreso).alRegistrar(boton.capture()));

        verify(panelIngreso).mostrarError(Acciones.MENSAJE_ERROR_INESPERADO);
    }

    @Test
    @DisplayName("Sin placa no se llama al servicio")
    void placaVacia() {
        when(panelIngreso.getPlaca()).thenReturn("   ");

        presionar(boton -> verify(panelIngreso).alRegistrar(boton.capture()));

        verify(panelIngreso).mostrarError("Ingrese la placa del vehículo.");
        verify(servicio, never()).registrarIngreso(any(), any());
    }

    @Test
    @DisplayName("Registrar salida: muestra la liquidación y refresca la lista")
    void registrarSalida() {
        when(panelSalida.getPlaca()).thenReturn("ABC123");
        when(servicio.registrarSalida("ABC123")).thenReturn(pendiente);

        presionar(boton -> verify(panelSalida).alRegistrarSalida(boton.capture()));

        verify(panelSalida).mostrarLiquidacion(pendiente);
        verify(panelSalida).limpiar();
        verify(panelVehiculos).mostrarEnParqueadero(enParqueadero);
    }

    @Test
    @DisplayName("Consultar valor (CU-01): muestra el valor en el panel de pago")
    void consultarValor() {
        when(panelPagoCaja.getPlaca()).thenReturn("ABC123");
        when(servicio.consultarValor("ABC123")).thenReturn(pendiente);

        presionar(boton -> verify(panelPagoCaja).alConsultar(boton.capture()));

        verify(panelPagoCaja).mostrarValor(pendiente);
    }

    @Test
    @DisplayName("Registrar pago en caja: muestra el pago, limpia y refresca la lista")
    void registrarPago() {
        EstadiaDTO cerrada = new EstadiaDTO("ABC123", "Automóvil", INGRESO, INGRESO.plusMinutes(160),
                new BigDecimal("5400"), "CERRADA");
        when(panelPagoCaja.getPlaca()).thenReturn("ABC123");
        when(servicio.registrarPagoEnCaja("ABC123")).thenReturn(cerrada);

        presionar(boton -> verify(panelPagoCaja).alPagar(boton.capture()));

        verify(panelPagoCaja).mostrarPagoRegistrado(cerrada);
        verify(panelPagoCaja).limpiar();
        verify(panelVehiculos).mostrarEnParqueadero(enParqueadero);
    }

    @Test
    @DisplayName("El botón «Actualizar listas» vuelve a cargar los vehículos en el parqueadero")
    void actualizarLista() {
        presionar(boton -> verify(panelVehiculos).alActualizar(boton.capture()));

        verify(panelVehiculos).mostrarEnParqueadero(enParqueadero);
    }

    // Recupera la acción que el controlador registró en el botón y la ejecuta, como si el usuario lo presionara.
    // Uso: presionar(boton -> verify(panel).alRegistrar(boton.capture()));
    static void presionar(Consumer<ArgumentCaptor<Runnable>> verificarRegistro) {
        ArgumentCaptor<Runnable> boton = ArgumentCaptor.forClass(Runnable.class);
        verificarRegistro.accept(boton);
        boton.getValue().run();
    }
}
