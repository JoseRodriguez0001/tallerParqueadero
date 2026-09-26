package com.parqueadero.presentacion.componentes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import com.parqueadero.negocio.dto.EstadiaDTO;

// Tabla de estadías (CU-07, vista "En el parqueadero")
public class EstadiaTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = { "Placa", "Tipo", "Ingreso", "Salida", "Valor", "Estado" };
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            case 2 -> formatearFecha(estadia.fechaIngreso());
            case 3 -> formatearFecha(estadia.fechaSalida());
            case 4 -> formatearValor(estadia.valor());
            case 5 -> formatearEstado(estadia.estado());
            default -> throw new IllegalArgumentException("Columna inexistente: " + columna);
        };
    }

    private static String formatearFecha(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    private static String formatearValor(BigDecimal valor) {
        return valor == null ? "" : String.format(Locale.forLanguageTag("es-CO"), "$%,.0f", valor);
    }

    private static String formatearEstado(String estado) {
        return switch (estado) {
            case "DENTRO" -> "Dentro";
            case "PENDIENTE_PAGO" -> "Pendiente de pago";
            case "CERRADA" -> "Cerrada";
            default -> estado;
        };
    }
}
