package com.parqueadero.negocio.servicio;

import java.util.List;

import com.parqueadero.negocio.dto.VehiculoDTO;

public interface VehiculoService {

    VehiculoDTO registrar(String placa, String codigoTipo);

    List<VehiculoDTO> listarRegistrados();
}
