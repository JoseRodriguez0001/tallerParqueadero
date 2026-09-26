package com.parqueadero.presentacion.controlador;

import java.util.List;

import com.parqueadero.negocio.dto.TipoVehiculoDTO;
import com.parqueadero.negocio.dto.VehiculoDTO;
import com.parqueadero.negocio.servicio.TipoVehiculoService;
import com.parqueadero.negocio.servicio.VehiculoService;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelVehiculos;

// CU-04 Registrar vehículo, CU-07 (vista "Registrados") y los selectores de tipo de vehículo
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final TipoVehiculoService tipoVehiculoService;
    private final PanelVehiculos panelVehiculos;
    private final PanelIngreso panelIngreso;

    public VehiculoController(VehiculoService vehiculoService, TipoVehiculoService tipoVehiculoService,
            PanelVehiculos panelVehiculos, PanelIngreso panelIngreso) {
        this.vehiculoService = vehiculoService;
        this.tipoVehiculoService = tipoVehiculoService;
        this.panelVehiculos = panelVehiculos;
        this.panelIngreso = panelIngreso;

        panelVehiculos.alRegistrarVehiculo(this::registrarVehiculo);
        panelVehiculos.alActualizar(this::actualizarRegistrados);
        panelVehiculos.alMostrarse(this::actualizarRegistrados);
    }

    // Carga inicial al abrir la aplicación
    public void iniciar() {
        Acciones.ejecutar(() -> {
            List<TipoVehiculoDTO> tipos = tipoVehiculoService.listarActivos();
            panelVehiculos.setTiposVehiculo(tipos);
            panelIngreso.setTiposVehiculo(tipos);
            panelVehiculos.mostrarRegistrados(vehiculoService.listarRegistrados());
        }, panelVehiculos::mostrarError);
    }

    private void registrarVehiculo() {
        Acciones.ejecutar(() -> {
            String placa = Acciones.obligatorio(panelVehiculos.getPlaca(), "Ingrese la placa del vehículo.");
            String codigoTipo = Acciones.obligatorio(panelVehiculos.getCodigoTipo(), "Seleccione el tipo de vehículo.");

            VehiculoDTO registrado = vehiculoService.registrar(placa, codigoTipo);

            panelVehiculos.mostrarInformacion(
                    "Vehículo " + registrado.placa() + " registrado como " + registrado.tipo() + ".");
            panelVehiculos.limpiarFormulario();
            panelVehiculos.mostrarRegistrados(vehiculoService.listarRegistrados());
        }, panelVehiculos::mostrarError);
    }

    private void actualizarRegistrados() {
        Acciones.ejecutar(
                () -> panelVehiculos.mostrarRegistrados(vehiculoService.listarRegistrados()),
                panelVehiculos::mostrarError);
    }
}
