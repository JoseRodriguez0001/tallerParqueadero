package com.parqueadero.negocio.servicio;

import java.util.List;

import com.parqueadero.negocio.dto.EstadiaDTO;

public interface EstadiaService {
    EstadiaDTO registrarIngreso(String placa, String codigoTipo);

    EstadiaDTO consultarValor(String placa);

    EstadiaDTO registrarSalida(String placa);

    EstadiaDTO registrarPagoEnCaja(String placa);

    List<EstadiaDTO> listarEnParqueadero();
}
