package com.parqueadero.presentacion.controlador;

import static com.parqueadero.presentacion.controlador.EstadiaControllerTest.presionar;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;
import com.parqueadero.negocio.dto.VehiculoDTO;
import com.parqueadero.negocio.servicio.TipoVehiculoService;
import com.parqueadero.negocio.servicio.VehiculoService;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelVehiculos;

class VehiculoControllerTest {

    private final List<TipoVehiculoDTO> tipos = List.of(
            new TipoVehiculoDTO("MOTO", "Motocicleta"), new TipoVehiculoDTO("CARRO", "Automóvil"));
    private final List<VehiculoDTO> registrados = List.of(new VehiculoDTO("ABC123", "Automóvil", true));

    private VehiculoService vehiculoService;
    private TipoVehiculoService tipoVehiculoService;
    private PanelVehiculos panelVehiculos;
    private PanelIngreso panelIngreso;
    private VehiculoController controlador;

    @BeforeEach
    void setUp() {
        vehiculoService = mock(VehiculoService.class);
        tipoVehiculoService = mock(TipoVehiculoService.class);
        panelVehiculos = mock(PanelVehiculos.class);
        panelIngreso = mock(PanelIngreso.class);

        when(tipoVehiculoService.listarActivos()).thenReturn(tipos);
        when(vehiculoService.listarRegistrados()).thenReturn(registrados);

        controlador = new VehiculoController(vehiculoService, tipoVehiculoService, panelVehiculos, panelIngreso);
    }

    @Test
    @DisplayName("Al iniciar carga los tipos en los dos selectores y la lista de registrados")
    void iniciar() {
        controlador.iniciar();

        verify(panelVehiculos).setTiposVehiculo(tipos);
        verify(panelIngreso).setTiposVehiculo(tipos);
        verify(panelVehiculos).mostrarRegistrados(registrados);
    }

    @Test
    @DisplayName("CU-04: registra el vehículo, informa, limpia el formulario y refresca la lista")
    void registrarVehiculo() {
        when(panelVehiculos.getPlaca()).thenReturn("abc-123");
        when(panelVehiculos.getCodigoTipo()).thenReturn("CARRO");
        when(vehiculoService.registrar("abc-123", "CARRO")).thenReturn(new VehiculoDTO("ABC123", "Automóvil", false));

        presionar(boton -> verify(panelVehiculos).alRegistrarVehiculo(boton.capture()));

        verify(panelVehiculos).mostrarInformacion(contains("ABC123"));
        verify(panelVehiculos).limpiarFormulario();
        verify(panelVehiculos).mostrarRegistrados(registrados);
    }

    @Test
    @DisplayName("Sin tipo seleccionado no se llama al servicio")
    void sinTipo() {
        when(panelVehiculos.getPlaca()).thenReturn("ABC123");
        when(panelVehiculos.getCodigoTipo()).thenReturn(null);

        presionar(boton -> verify(panelVehiculos).alRegistrarVehiculo(boton.capture()));

        verify(panelVehiculos).mostrarError("Seleccione el tipo de vehículo.");
        verify(vehiculoService, never()).registrar(any(), any());
    }

    @Test
    @DisplayName("Si el servicio rechaza el registro, se muestra su mensaje y no se limpia el formulario")
    void registroRechazado() {
        when(panelVehiculos.getPlaca()).thenReturn("ABC123");
        when(panelVehiculos.getCodigoTipo()).thenReturn("CARRO");
        when(vehiculoService.registrar(anyString(), anyString()))
                .thenThrow(new NegocioException("La placa ABC123 ya está registrada."));

        presionar(boton -> verify(panelVehiculos).alRegistrarVehiculo(boton.capture()));

        verify(panelVehiculos).mostrarError("La placa ABC123 ya está registrada.");
        verify(panelVehiculos, never()).limpiarFormulario();
    }

    @Test
    @DisplayName("El botón «Actualizar listas» vuelve a cargar los registrados")
    void actualizarLista() {
        presionar(boton -> verify(panelVehiculos).alActualizar(boton.capture()));

        verify(panelVehiculos).mostrarRegistrados(registrados);
    }
}
