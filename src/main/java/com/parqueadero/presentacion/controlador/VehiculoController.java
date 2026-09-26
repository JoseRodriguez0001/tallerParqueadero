package com.parqueadero.presentacion.controlador;

import com.parqueadero.negocio.servicio.TipoVehiculoService;
import com.parqueadero.negocio.servicio.VehiculoService;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelVehiculos;

// CU-04 Registrar vehículo y CU-07 (vista "Registrados"); llena los selectores de tipo de vehículo
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
    }
}
