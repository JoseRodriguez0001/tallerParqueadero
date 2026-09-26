package com.parqueadero.dominio.cobro;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parqueadero.dominio.modelo.Tarifa;

public interface PoliticaCobro {
    BigDecimal calcular(Tarifa tarifa, LocalDateTime ingreso, LocalDateTime salida);
}
