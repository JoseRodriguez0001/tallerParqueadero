package com.parqueadero.presentacion.componentes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Cómo se muestran fechas, dinero y estados en la interfaz (los DTO traen los datos sin formato)
public final class Formatos {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private Formatos() {
    }

    public static String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    public static String dinero(BigDecimal valor) {
        return valor == null ? "" : String.format(COLOMBIA, "$%,.0f", valor);
    }

    public static String estado(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "DENTRO" -> "Dentro";
            case "PENDIENTE_PAGO" -> "Pendiente de pago";
            case "CERRADA" -> "Cerrada";
            default -> estado;
        };
    }
}
