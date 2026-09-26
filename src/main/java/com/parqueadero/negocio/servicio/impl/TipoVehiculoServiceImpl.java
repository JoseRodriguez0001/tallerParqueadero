package com.parqueadero.negocio.servicio.impl;

import java.util.List;

import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.datos.transaccion.GestorTransacciones;
import com.parqueadero.negocio.dto.TipoVehiculoDTO;
import com.parqueadero.negocio.mapper.TipoVehiculoMapper;
import com.parqueadero.negocio.servicio.TipoVehiculoService;

public class TipoVehiculoServiceImpl implements TipoVehiculoService {

    private final GestorTransacciones gestor;
    private final TipoVehiculoRepository tipos;

    public TipoVehiculoServiceImpl(GestorTransacciones gestor, TipoVehiculoRepository tipos) {
        this.gestor = gestor;
        this.tipos = tipos;
    }

    // Llena los selectores de tipo de las pantallas de registro e ingreso
    @Override
    public List<TipoVehiculoDTO> listarActivos() {

        return gestor.ejecutar(() -> tipos.listarActivos().stream()
                .map(TipoVehiculoMapper::aDTO)
                .toList());
    }
}
