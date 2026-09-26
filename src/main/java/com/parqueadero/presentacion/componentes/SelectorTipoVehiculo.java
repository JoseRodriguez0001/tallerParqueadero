package com.parqueadero.presentacion.componentes;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import com.parqueadero.negocio.dto.TipoVehiculoDTO;

// Desplegable de tipos de vehículo: muestra el nombre y entrega el código; sin selección muestra un texto de ayuda
public class SelectorTipoVehiculo extends JComboBox<TipoVehiculoDTO> {

    private final DefaultComboBoxModel<TipoVehiculoDTO> modelo = new DefaultComboBoxModel<>();

    public SelectorTipoVehiculo(String textoSinSeleccion) {
        setModel(modelo);
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean conFoco) {
                String texto = valor == null ? textoSinSeleccion : ((TipoVehiculoDTO) valor).nombre();
                return super.getListCellRendererComponent(lista, texto, indice, seleccionado, conFoco);
            }
        });
    }

    public void setTipos(List<TipoVehiculoDTO> tipos) {
        modelo.removeAllElements();
        modelo.addAll(tipos);
        limpiar();
    }

    // Código del tipo elegido, o null si no se eligió ninguno
    public String getCodigoSeleccionado() {
        TipoVehiculoDTO seleccionado = (TipoVehiculoDTO) getSelectedItem();
        return seleccionado == null ? null : seleccionado.codigo();
    }

    public void limpiar() {
        setSelectedIndex(-1);
    }
}
