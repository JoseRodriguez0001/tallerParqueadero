package com.parqueadero.presentacion.vista;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;
import com.parqueadero.presentacion.componentes.Formatos;
import com.parqueadero.presentacion.componentes.SelectorTipoVehiculo;

// CU-03 Registrar ingreso: placa y, si la placa no está registrada, tipo de vehículo (CU-04 extend)
public class PanelIngreso extends PanelBase {

    private final JTextField campoPlaca = new JTextField(12);
    private final SelectorTipoVehiculo selectorTipo = new SelectorTipoVehiculo("Solo si la placa es nueva");
    private final JButton botonRegistrar = new JButton("Registrar ingreso");
    private final JLabel resultado = areaDeResultado();

    public PanelIngreso() {
        JPanel entrada = new JPanel(new BorderLayout());
        entrada.add(formulario("Ingreso de vehículo",
                "Placa:", campoPlaca,
                "Tipo de vehículo:", selectorTipo), BorderLayout.CENTER);
        entrada.add(filaDeBotones(botonRegistrar), BorderLayout.SOUTH);

        add(entrada, BorderLayout.NORTH);
        add(resultado, BorderLayout.CENTER);

        campoPlaca.addActionListener(evento -> botonRegistrar.doClick());   // Enter en la placa registra
    }

    public String getPlaca() {
        return campoPlaca.getText();
    }

    public String getCodigoTipo() {
        return selectorTipo.getCodigoSeleccionado();
    }

    public void setTiposVehiculo(List<TipoVehiculoDTO> tipos) {
        selectorTipo.setTipos(tipos);
    }

    public void alRegistrar(Runnable accion) {
        botonRegistrar.addActionListener(evento -> accion.run());
    }

    public void mostrarIngreso(EstadiaDTO estadia) {
        resultado.setText(html("Ingreso registrado",
                "Placa: " + estadia.placa(),
                "Tipo: " + estadia.tipo(),
                "Hora de ingreso: " + Formatos.fecha(estadia.fechaIngreso()),
                "Estado: " + Formatos.estado(estadia.estado())));
    }

    // Limpia los datos de entrada; el resultado queda visible
    public void limpiar() {
        campoPlaca.setText("");
        selectorTipo.limpiar();
        campoPlaca.requestFocusInWindow();
    }
}
