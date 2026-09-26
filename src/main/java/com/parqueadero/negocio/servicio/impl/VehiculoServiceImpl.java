package com.parqueadero.negocio.servicio.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.datos.repositorio.EstadiaRepository;
import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.datos.repositorio.VehiculoRepository;
import com.parqueadero.datos.transaccion.GestorTransacciones;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Vehiculo;
import com.parqueadero.negocio.dto.VehiculoDTO;
import com.parqueadero.negocio.mapper.VehiculoMapper;
import com.parqueadero.negocio.servicio.VehiculoService;

public class VehiculoServiceImpl implements VehiculoService {

    private final GestorTransacciones gestor;
    private final VehiculoRepository vehiculos;
    private final TipoVehiculoRepository tipos;
    private final EstadiaRepository estadias;

    public VehiculoServiceImpl(GestorTransacciones gestor, VehiculoRepository vehiculos,
            TipoVehiculoRepository tipos, EstadiaRepository estadias) {
        this.gestor = gestor;
        this.vehiculos = vehiculos;
        this.tipos = tipos;
        this.estadias = estadias;
    }

    @Override
    public VehiculoDTO registrar(String placa, String codigoTipo) {

        return gestor.ejecutar(() -> {
            String placaNormalizada = Vehiculo.normalizarPlaca(placa);

            if (vehiculos.buscarPorPlaca(placaNormalizada).isPresent()) {
                throw new NegocioException("La placa " + placaNormalizada + " ya está registrada.");
            }

            TipoVehiculo tipo = tipos.buscarPorCodigo(codigoTipo)
                    .orElseThrow(() -> new NegocioException("El tipo de vehículo " + codigoTipo + " no existe."));

            // El constructor valida la placa y que el tipo esté activo
            Vehiculo guardado = vehiculos.guardar(new Vehiculo(placaNormalizada, tipo));

            // Recién registrado: todavía no tiene estadía
            return VehiculoMapper.aDTO(guardado, false);
        });
    }

    @Override
    public List<VehiculoDTO> listarRegistrados() {

        return gestor.ejecutar(() -> {
            // Activa = distinta de CERRADA, incluye PENDIENTE_PAGO (mismo criterio que "En el parqueadero")
            Set<String> dentro = estadias.listarActivas().stream()
                    .map(estadia -> estadia.getVehiculo().getPlaca())
                    .collect(Collectors.toSet());

            return vehiculos.listarTodos().stream()
                    .map(vehiculo -> VehiculoMapper.aDTO(vehiculo, dentro.contains(vehiculo.getPlaca())))
                    .toList();
        });
    }
}
