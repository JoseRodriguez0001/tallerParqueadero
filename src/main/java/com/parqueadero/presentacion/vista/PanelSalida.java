package com.parqueadero.presentacion.vista;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.presentacion.componentes.Formatos;

// CU-05 Registrar salida: liquida la estadía y muestra el valor a pagar
public class PanelSalida extends PanelBase {

    private final JTextField campoPlaca = new JTextField(12);
    private final JButton botonRegistrar = new JButton("Registrar salida");
    private final JLabel resultado = areaDeResultado();

    public PanelSalida() {
        JPanel entrada = new JPanel(new BorderLayout());
        entrada.add(formulario("Salida de vehículo", "Placa:", campoPlaca), BorderLayout.CENTER);
        entrada.add(filaDeBotones(botonRegistrar), BorderLayout.SOUTH);

        add(entrada, BorderLayout.NORTH);
        add(resultado, BorderLayout.CENTER);

        campoPlaca.addActionListener(evento -> botonRegistrar.doClick());
    }

    public String getPlaca() {
        return campoPlaca.getText();
    }

    public void alRegistrarSalida(Runnable accion) {
        botonRegistrar.addActionListener(evento -> accion.run());
    }

    public void mostrarLiquidacion(EstadiaDTO estadia) {
        resultado.setText(html("Salida registrada",
                "Placa: " + estadia.placa(),
                "Tipo: " + estadia.tipo(),
                "Hora de ingreso: " + Formatos.fecha(estadia.fechaIngreso()),
                "Hora de salida: " + Formatos.fecha(estadia.fechaSalida()),
                "Valor a pagar: " + Formatos.dinero(estadia.valor()),
                "Estado: " + Formatos.estado(estadia.estado()),
                "",
                "El vehículo puede salir una vez se registre el pago (pestaña «Pago en caja»)."));
    }

    public void limpiar() {
        campoPlaca.setText("");
        campoPlaca.requestFocusInWindow();
    }
}
