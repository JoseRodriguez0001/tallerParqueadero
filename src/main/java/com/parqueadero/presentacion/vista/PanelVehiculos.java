package com.parqueadero.presentacion.vista;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;
import com.parqueadero.negocio.dto.VehiculoDTO;
import com.parqueadero.presentacion.componentes.EstadiaTableModel;
import com.parqueadero.presentacion.componentes.SelectorTipoVehiculo;
import com.parqueadero.presentacion.componentes.VehiculoTableModel;

// CU-04 Registrar vehículo y CU-07 Consultar vehículos ("En el parqueadero" y "Registrados").
// Lo comparten dos controladores: EstadiaController llena "En el parqueadero" y VehiculoController el resto.
public class PanelVehiculos extends PanelBase {

    private final JTextField campoPlaca = new JTextField(12);
    private final SelectorTipoVehiculo selectorTipo = new SelectorTipoVehiculo("Seleccione el tipo");
    private final JButton botonRegistrar = new JButton("Registrar vehículo");
    private final JButton botonActualizar = new JButton("Actualizar listas");

    private final EstadiaTableModel modeloEnParqueadero = new EstadiaTableModel();
    private final VehiculoTableModel modeloRegistrados = new VehiculoTableModel();

    public PanelVehiculos() {
        JPanel registro = new JPanel(new BorderLayout());
        registro.add(formulario("Registrar vehículo",
                "Placa:", campoPlaca,
                "Tipo de vehículo:", selectorTipo), BorderLayout.CENTER);
        registro.add(filaDeBotones(botonRegistrar), BorderLayout.SOUTH);

        JTabbedPane listas = new JTabbedPane();
        listas.addTab("En el parqueadero", new JScrollPane(new JTable(modeloEnParqueadero)));
        listas.addTab("Registrados", new JScrollPane(new JTable(modeloRegistrados)));

        add(registro, BorderLayout.NORTH);
        add(listas, BorderLayout.CENTER);
        add(filaDeBotones(botonActualizar), BorderLayout.SOUTH);

        campoPlaca.addActionListener(evento -> botonRegistrar.doClick());
    }

    // ── Registro (CU-04) ───────────────────────────────────────────────────────

    public String getPlaca() {
        return campoPlaca.getText();
    }

    public String getCodigoTipo() {
        return selectorTipo.getCodigoSeleccionado();
    }

    public void setTiposVehiculo(List<TipoVehiculoDTO> tipos) {
        selectorTipo.setTipos(tipos);
    }

    public void alRegistrarVehiculo(Runnable accion) {
        botonRegistrar.addActionListener(evento -> accion.run());
    }

    public void limpiarFormulario() {
        campoPlaca.setText("");
        selectorTipo.limpiar();
        campoPlaca.requestFocusInWindow();
    }

    // ── Listas (CU-07) ─────────────────────────────────────────────────────────

    // Varios controladores pueden registrarse: cada uno actualiza su lista
    public void alActualizar(Runnable accion) {
        botonActualizar.addActionListener(evento -> accion.run());
    }

    public void mostrarEnParqueadero(List<EstadiaDTO> estadias) {
        modeloEnParqueadero.setEstadias(estadias);
    }

    public void mostrarRegistrados(List<VehiculoDTO> vehiculos) {
        modeloRegistrados.setVehiculos(vehiculos);
    }
}
