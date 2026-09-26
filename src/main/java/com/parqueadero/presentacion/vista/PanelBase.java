package com.parqueadero.presentacion.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Base de los paneles: mensajes al usuario, armado de formularios y aviso cuando el panel se hace visible
public abstract class PanelBase extends JPanel {

    protected PanelBase() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operación", JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Operación realizada", JOptionPane.INFORMATION_MESSAGE);
    }

    // Se ejecuta cada vez que el panel se hace visible, por ejemplo al seleccionar su pestaña
    public void alMostrarse(Runnable accion) {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent evento) {
                accion.run();
            }
        });
    }

    // ── Ayudas para construir los paneles ──────────────────────────────────────

    // Formulario de dos columnas: etiqueta, componente, etiqueta, componente...
    protected static JPanel formulario(String titulo, Object... etiquetasYComponentes) {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder(titulo));

        GridBagConstraints posicion = new GridBagConstraints();
        posicion.insets = new Insets(6, 6, 6, 6);
        posicion.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < etiquetasYComponentes.length; i += 2) {
            posicion.gridy = i / 2;

            posicion.gridx = 0;
            posicion.weightx = 0;
            posicion.fill = GridBagConstraints.NONE;
            formulario.add(new JLabel((String) etiquetasYComponentes[i]), posicion);

            posicion.gridx = 1;
            posicion.weightx = 1;
            posicion.fill = GridBagConstraints.HORIZONTAL;
            formulario.add((Component) etiquetasYComponentes[i + 1], posicion);
        }
        return formulario;
    }

    protected static JPanel filaDeBotones(JButton... botones) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton boton : botones) {
            fila.add(boton);
        }
        return fila;
    }

    protected static JLabel areaDeResultado() {
        JLabel resultado = new JLabel();
        resultado.setVerticalAlignment(SwingConstants.TOP);
        resultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resultado"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return resultado;
    }

    // Texto con formato para el área de resultado; cada línea es "Etiqueta: valor" o un texto libre
    protected static String html(String titulo, String... lineas) {
        StringBuilder texto = new StringBuilder("<html><h2>").append(escapar(titulo)).append("</h2>");
        for (String linea : lineas) {
            texto.append(escapar(linea)).append("<br>");
        }
        return texto.append("</html>").toString();
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
