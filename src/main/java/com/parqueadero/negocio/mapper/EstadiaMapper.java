package com.parqueadero.negocio.mapper;

import java.math.BigDecimal;

import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.negocio.dto.EstadiaDTO;

public final class EstadiaMapper {

    private EstadiaMapper() {
    }

    // valor: lo calcula el servicio (estimado si está DENTRO, definitivo si no); el mapper no aplica reglas
    public static EstadiaDTO aDTO(Estadia estadia, BigDecimal valor) {
        return new EstadiaDTO(
                estadia.getVehiculo().getPlaca(),
                estadia.getVehiculo().getTipoVehiculo().getNombre(),
                estadia.getFechaIngreso(),
                estadia.getFechaSalida().orElse(null),
                valor,
                estadia.getEstado().name());
    }
}
