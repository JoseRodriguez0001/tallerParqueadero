package com.parqueadero.presentacion.componentes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.parqueadero.negocio.dto.VehiculoDTO;

public class VehiculoTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = { "Placa", "Tipo", "En el parqueadero" };

    private List<VehiculoDTO> vehiculos = new ArrayList<>();

    public void setVehiculos(List<VehiculoDTO> vehiculos) {
        this.vehiculos = new ArrayList<>(vehiculos);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return vehiculos.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        VehiculoDTO vehiculo = vehiculos.get(fila);
        return switch (columna) {
            case 0 -> vehiculo.placa();
            case 1 -> vehiculo.tipo();
            case 2 -> vehiculo.dentro() ? "Sí" : "No";
            default -> throw new IllegalArgumentException("Columna inexistente: " + columna);
        };
    }
}
