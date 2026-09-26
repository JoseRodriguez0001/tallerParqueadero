package com.parqueadero.presentacion.componentes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.parqueadero.negocio.dto.EstadiaDTO;

// Tabla de estadías (CU-07, vista "En el parqueadero")
public class EstadiaTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = { "Placa", "Tipo", "Ingreso", "Salida", "Valor", "Estado" };

    private List<EstadiaDTO> estadias = new ArrayList<>();

    public void setEstadias(List<EstadiaDTO> estadias) {
        this.estadias = new ArrayList<>(estadias);
        fireTableDataChanged();
    }

    public EstadiaDTO getEstadia(int fila) {
        return estadias.get(fila);
    }

    @Override
    public int getRowCount() {
        return estadias.size();
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
        EstadiaDTO estadia = estadias.get(fila);
        return switch (columna) {
            case 0 -> estadia.placa();
            case 1 -> estadia.tipo();
            case 2 -> Formatos.fecha(estadia.fechaIngreso());
            case 3 -> Formatos.fecha(estadia.fechaSalida());
            case 4 -> Formatos.dinero(estadia.valor());
            case 5 -> Formatos.estado(estadia.estado());
            default -> throw new IllegalArgumentException("Columna inexistente: " + columna);
        };
    }
}
