package com.parqueadero.dominio.cobro;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parqueadero.dominio.modelo.Tarifa;

public class CobroPorHoraIniciada implements PoliticaCobro {

    @Override
    public BigDecimal calcular(Tarifa tarifa, LocalDateTime ingreso, LocalDateTime salida) {

        throw new UnsupportedOperationException("Unimplemented method 'calcular'");
    }

}
