package com.parqueadero.negocio.servicio;

import java.util.List;

import com.parqueadero.negocio.dto.TipoVehiculoDTO;

public interface TipoVehiculoService {

    List<TipoVehiculoDTO> listarActivos();
}
