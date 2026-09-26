package com.parqueadero.datos.repositorio;

import java.util.List;
import java.util.Optional;

import com.parqueadero.dominio.modelo.Estadia;

public interface EstadiaRepository {
    Optional<Estadia> buscarActivaPorPlaca(String placa);

    List<Estadia> listarActivas();

    Estadia guardar(Estadia estadia);

    void actualizar(Estadia estadia);
}
