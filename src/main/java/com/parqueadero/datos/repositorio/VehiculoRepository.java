package com.parqueadero.datos.repositorio;

import java.util.List;
import java.util.Optional;

import com.parqueadero.dominio.modelo.Vehiculo;

public interface VehiculoRepository {
    Optional<Vehiculo> buscarPorPlaca(String placa);

    Vehiculo guardar(Vehiculo vehiculo);

    List<Vehiculo> listarTodos();
}
