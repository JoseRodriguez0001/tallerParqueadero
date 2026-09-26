package com.parqueadero.dominio.cobro;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import com.parqueadero.dominio.modelo.Tarifa;

public class CobroPorHoraIniciada implements PoliticaCobro {

    @Override
    public BigDecimal calcular(Tarifa tarifa, LocalDateTime ingreso, LocalDateTime salida) {

        if (salida.isBefore(ingreso)) {
            throw new IllegalArgumentException("La salida no puede ser anterior al ingreso.");
        }

        long minutos = Duration.between(ingreso, salida).toMinutes();

        // toda hora iniciada se cobra completa, con mínimo de una hora
        long horas = Math.max(1, (minutos + 59) / 60);

        return tarifa.getValorHora().multiply(BigDecimal.valueOf(horas));
    }

}
