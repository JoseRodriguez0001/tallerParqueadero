package com.parqueadero.negocio.mapper;

import com.parqueadero.dominio.modelo.Vehiculo;
import com.parqueadero.negocio.dto.VehiculoDTO;

public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    // dentro: lo decide el servicio (si tiene una estadía activa); el mapper no consulta nada
    public static VehiculoDTO aDTO(Vehiculo vehiculo, boolean dentro) {
        return new VehiculoDTO(vehiculo.getPlaca(), vehiculo.getTipoVehiculo().getNombre(), dentro);
    }
}
