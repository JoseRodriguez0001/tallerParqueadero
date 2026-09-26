package com.parqueadero.datos.repositorio;

import java.time.LocalDateTime;
import java.util.Optional;

import com.parqueadero.dominio.modelo.Tarifa;

public interface TarifaRepository {
    Optional<Tarifa> buscarVigente(int tipoVehiculoId, LocalDateTime fecha);
}
