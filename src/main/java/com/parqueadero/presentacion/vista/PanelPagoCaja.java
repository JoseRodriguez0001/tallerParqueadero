package com.parqueadero.presentacion.vista;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.presentacion.componentes.Formatos;

// CU-06 Registrar pago en caja: primero se consulta el valor (CU-01) y luego se registra el pago
public class PanelPagoCaja extends PanelBase {

    private final JTextField campoPlaca = new JTextField(12);
    private final JButton botonConsultar = new JButton("Consultar valor");
    private final JButton botonPagar = new JButton("Registrar pago");
    private final JLabel resultado = areaDeResultado();

    public PanelPagoCaja() {
        JPanel entrada = new JPanel(new BorderLayout());
        entrada.add(formulario("Pago en caja", "Placa:", campoPlaca), BorderLayout.CENTER);
        entrada.add(filaDeBotones(botonConsultar, botonPagar), BorderLayout.SOUTH);

        add(entrada, BorderLayout.NORTH);
        add(resultado, BorderLayout.CENTER);

        botonPagar.setEnabled(false);
        campoPlaca.addActionListener(evento -> botonConsultar.doClick());
        // Si cambia la placa, el valor consultado ya no aplica: hay que volver a consultar antes de pagar
        campoPlaca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                botonPagar.setEnabled(false);
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                botonPagar.setEnabled(false);
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                botonPagar.setEnabled(false);
            }
        });
    }

    public String getPlaca() {
        return campoPlaca.getText();
    }

    public void alConsultar(Runnable accion) {
        botonConsultar.addActionListener(evento -> accion.run());
    }

    public void alPagar(Runnable accion) {
        botonPagar.addActionListener(evento -> accion.run());
    }

    // RN-13: si la estadía sigue dentro, el valor es un estimado y todavía no se puede pagar
    public void mostrarValor(EstadiaDTO estadia) {
        boolean liquidada = "PENDIENTE_PAGO".equals(estadia.estado());

        if (liquidada) {
            resultado.setText(html("Valor a pagar: " + Formatos.dinero(estadia.valor()),
                    "Placa: " + estadia.placa(),
                    "Tipo: " + estadia.tipo(),
                    "Hora de ingreso: " + Formatos.fecha(estadia.fechaIngreso()),
                    "Hora de salida: " + Formatos.fecha(estadia.fechaSalida()),
                    "",
                    "Reciba el pago y confirme con «Registrar pago»."));
        } else {
            resultado.setText(html("Valor estimado: " + Formatos.dinero(estadia.valor()),
                    "Placa: " + estadia.placa(),
                    "Hora de ingreso: " + Formatos.fecha(estadia.fechaIngreso()),
                    "",
                    "El vehículo sigue dentro: el valor es un estimado a la hora actual.",
                    "Primero registre la salida para liquidar el valor definitivo."));
        }
        botonPagar.setEnabled(liquidada);
    }

    public void mostrarPagoRegistrado(EstadiaDTO estadia) {
        resultado.setText(html("Pago registrado",
                "Placa: " + estadia.placa(),
                "Valor pagado: " + Formatos.dinero(estadia.valor()),
                "Estado: " + Formatos.estado(estadia.estado()),
                "",
                "La estadía quedó cerrada: el vehículo puede salir."));
        botonPagar.setEnabled(false);
    }

    public void limpiar() {
        campoPlaca.setText("");
        campoPlaca.requestFocusInWindow();
    }
}
