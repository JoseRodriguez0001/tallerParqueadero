package com.parqueadero.negocio.mapper;

import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;

public final class TipoVehiculoMapper {

    private TipoVehiculoMapper() {
    }

    public static TipoVehiculoDTO aDTO(TipoVehiculo tipo) {
        return new TipoVehiculoDTO(tipo.getCodigo(), tipo.getNombre());
    }
}
