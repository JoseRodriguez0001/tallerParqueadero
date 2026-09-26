package com.parqueadero.presentacion.controlador;

import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.negocio.servicio.EstadiaService;
import com.parqueadero.presentacion.vista.PanelIngreso;
import com.parqueadero.presentacion.vista.PanelPagoCaja;
import com.parqueadero.presentacion.vista.PanelSalida;
import com.parqueadero.presentacion.vista.PanelVehiculos;

public class EstadiaController {

    private static final String FALTA_PLACA = "Ingrese la placa del vehículo.";

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

        panelIngreso.alRegistrar(this::registrarIngreso);
        panelSalida.alRegistrarSalida(this::registrarSalida);
        panelPagoCaja.alConsultar(this::consultarValor);
        panelPagoCaja.alPagar(this::registrarPago);
        panelVehiculos.alActualizar(this::actualizarEnParqueadero);
        panelVehiculos.alMostrarse(this::actualizarEnParqueadero);
    }

    // Carga inicial al abrir la aplicación
    public void iniciar() {
        actualizarEnParqueadero();
    }

    private void registrarIngreso() {
        Acciones.ejecutar(() -> {
            String placa = Acciones.obligatorio(panelIngreso.getPlaca(), FALTA_PLACA);
            EstadiaDTO ingreso = servicio.registrarIngreso(placa, panelIngreso.getCodigoTipo());
            panelIngreso.mostrarIngreso(ingreso);
            panelIngreso.limpiar();
            panelVehiculos.mostrarEnParqueadero(servicio.listarEnParqueadero());
        }, panelIngreso::mostrarError);
    }

    private void registrarSalida() {
        Acciones.ejecutar(() -> {
            String placa = Acciones.obligatorio(panelSalida.getPlaca(), FALTA_PLACA);
            EstadiaDTO liquidada = servicio.registrarSalida(placa);
            panelSalida.mostrarLiquidacion(liquidada);
            panelSalida.limpiar();
            panelVehiculos.mostrarEnParqueadero(servicio.listarEnParqueadero());
        }, panelSalida::mostrarError);
    }

    private void consultarValor() {
        Acciones.ejecutar(() -> {
            String placa = Acciones.obligatorio(panelPagoCaja.getPlaca(), FALTA_PLACA);
            panelPagoCaja.mostrarValor(servicio.consultarValor(placa));
        }, panelPagoCaja::mostrarError);
    }

    private void registrarPago() {
        Acciones.ejecutar(() -> {
            String placa = Acciones.obligatorio(panelPagoCaja.getPlaca(), FALTA_PLACA);
            EstadiaDTO pagada = servicio.registrarPagoEnCaja(placa);
            panelPagoCaja.mostrarPagoRegistrado(pagada);
            panelPagoCaja.limpiar();
            panelVehiculos.mostrarEnParqueadero(servicio.listarEnParqueadero());
        }, panelPagoCaja::mostrarError);
    }

    private void actualizarEnParqueadero() {
        Acciones.ejecutar(
                () -> panelVehiculos.mostrarEnParqueadero(servicio.listarEnParqueadero()),
                panelVehiculos::mostrarError);
    }
}
