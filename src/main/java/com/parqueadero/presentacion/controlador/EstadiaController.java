package com.parqueadero.presentacion.controlador;

import com.parqueadero.negocio.servicio.EstadiaService;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelPagoCaja;
import com.parqueadero.presentacion.vista.PanelSalida;
import com.parqueadero.presentacion.vista.PanelVehiculos;

// CU-03, CU-05, CU-06 y CU-07 (vista "En el parqueadero")
public class EstadiaController {

    private final EstadiaService servicio;
    private final PanelIngreso panelIngreso;
    private final PanelSalida panelSalida;
    private final PanelPagoCaja panelPagoCaja;
    private final PanelVehiculos panelVehiculos;

    public EstadiaController(EstadiaService servicio, PanelIngreso panelIngreso, PanelSalida panelSalida,
            PanelPagoCaja panelPagoCaja, PanelVehiculos panelVehiculos) {
        this.servicio = servicio;
        this.panelIngreso = panelIngreso;
        this.panelSalida = panelSalida;
        this.panelPagoCaja = panelPagoCaja;
        this.panelVehiculos = panelVehiculos;
    }
}
