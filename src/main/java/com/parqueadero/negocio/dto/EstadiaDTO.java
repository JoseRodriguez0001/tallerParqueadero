package com.parqueadero.negocio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstadiaDTO(String placa, String tipo, LocalDateTime fechaIngreso,
        LocalDateTime fechaSalida, BigDecimal valor, String estado) {
}
