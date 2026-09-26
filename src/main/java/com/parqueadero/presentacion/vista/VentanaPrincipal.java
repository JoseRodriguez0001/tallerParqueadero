package com.parqueadero.presentacion.vista;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

// Ventana del personal del parqueadero: una pestaña por funcionalidad del prototipo
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(PanelVehiculos vehiculos, PanelIngreso ingreso,
            PanelSalida salida, PanelPagoCaja pagoCaja) {
        super("Sistema de Gestión de Parqueadero");

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Vehículos", vehiculos);
        pestanas.addTab("Registrar ingreso", ingreso);
        pestanas.addTab("Registrar salida", salida);
        pestanas.addTab("Pago en caja", pagoCaja);
        add(pestanas);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }
}
