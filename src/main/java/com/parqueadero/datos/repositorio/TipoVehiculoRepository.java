package com.parqueadero.datos.repositorio;

import java.util.List;
import java.util.Optional;

import com.parqueadero.dominio.modelo.TipoVehiculo;

public interface TipoVehiculoRepository {
    Optional<TipoVehiculo> buscarPorCodigo(String codigo);

    List<TipoVehiculo> listarActivos();
}
