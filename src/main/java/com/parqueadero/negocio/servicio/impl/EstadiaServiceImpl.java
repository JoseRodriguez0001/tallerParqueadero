
package com.parqueadero.negocio.servicio.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.datos.repositorio.EstadiaRepository;
import com.parqueadero.datos.repositorio.TarifaRepository;
import com.parqueadero.datos.repositorio.TipoVehiculoRepository;
import com.parqueadero.datos.repositorio.UsuarioRepository;
import com.parqueadero.datos.repositorio.VehiculoRepository;
import com.parqueadero.datos.transaccion.GestorTransacciones;
import com.parqueadero.dominio.cobro.PoliticaCobro;
import com.parqueadero.dominio.modelo.Estadia;
import com.parqueadero.dominio.modelo.Tarifa;
import com.parqueadero.dominio.modelo.TipoVehiculo;
import com.parqueadero.dominio.modelo.Usuario;
import com.parqueadero.dominio.modelo.Vehiculo;
import com.parqueadero.negocio.dto.EstadiaDTO;
import com.parqueadero.negocio.mapper.EstadiaMapper;
import com.parqueadero.negocio.servicio.EstadiaService;

public class EstadiaServiceImpl implements EstadiaService {

    private final GestorTransacciones gestor;
    private final EstadiaRepository estadias;
    private final VehiculoRepository vehiculos;
    private final TipoVehiculoRepository tipos;
    private final TarifaRepository tarifas;
    private final UsuarioRepository usuarios;
    private final PoliticaCobro politica;
    private final String usuarioCaja;
    private final Clock reloj;

    public EstadiaServiceImpl(GestorTransacciones gestor, EstadiaRepository estadias,
            VehiculoRepository vehiculos, TipoVehiculoRepository tipos, TarifaRepository tarifas,
            UsuarioRepository usuarios, PoliticaCobro politica, String usuarioCaja, Clock reloj) {
        this.gestor = gestor;
        this.estadias = estadias;
        this.vehiculos = vehiculos;
        this.tipos = tipos;
        this.tarifas = tarifas;
        this.usuarios = usuarios;
        this.politica = politica;
        this.usuarioCaja = usuarioCaja;
        this.reloj = reloj;
    }

    @Override
    public EstadiaDTO registrarIngreso(String placa, String codigoTipo) {

        return gestor.ejecutar(() -> {
            LocalDateTime ahora = LocalDateTime.now(reloj);
            String placaNormalizada = Vehiculo.normalizarPlaca(placa);

            Vehiculo vehiculo = vehiculoParaIngreso(placaNormalizada, codigoTipo);

            if (estadias.buscarActivaPorPlaca(placaNormalizada).isPresent()) {
                throw new NegocioException("El vehículo " + placaNormalizada
                        + " ya tiene una estadía activa: debe registrar su salida y pago antes de un nuevo ingreso.");
            }

            TipoVehiculo tipo = vehiculo.getTipoVehiculo();
            Tarifa tarifa = tarifas.buscarVigente(tipo.getId(), ahora)
                    .orElseThrow(() -> new NegocioException(
                            "No hay una tarifa vigente para " + tipo.getNombre() + "."));

            Estadia guardada = estadias.guardar(Estadia.iniciar(vehiculo, tarifa, ahora));

            return aDTO(guardada, ahora);
        });
    }

    private Vehiculo vehiculoParaIngreso(String placa, String codigoTipo) {

        Optional<Vehiculo> existente = vehiculos.buscarPorPlaca(placa);
        if (existente.isPresent()) {
            return existente.get();
        }

        if (codigoTipo == null || codigoTipo.isBlank()) {
            throw new NegocioException("La placa " + placa + " no está registrada: seleccione el tipo de vehículo.");
        }

        TipoVehiculo tipo = tipos.buscarPorCodigo(codigoTipo)
                .orElseThrow(() -> new NegocioException("El tipo de vehículo " + codigoTipo + " no existe."));

        return vehiculos.guardar(new Vehiculo(placa, tipo));
    }

    @Override
    public EstadiaDTO consultarValor(String placa) {

        return gestor.ejecutar(() -> {
            LocalDateTime ahora = LocalDateTime.now(reloj);
            Estadia estadia = estadiaActiva(placa);

            return aDTO(estadia, ahora);
        });

    }

    @Override
    public EstadiaDTO registrarSalida(String placa) {

        return gestor.ejecutar(() -> {
            LocalDateTime ahora = LocalDateTime.now(reloj);

            Estadia estadia = estadiaActiva(placa);
            estadia.registrarSalida(ahora, politica);
            estadias.actualizar(estadia);
            return aDTO(estadia, ahora);
        });

    }

    @Override
    public EstadiaDTO registrarPagoEnCaja(String placa) {

        return gestor.ejecutar(() -> {
            LocalDateTime ahora = LocalDateTime.now(reloj);

            Estadia estadia = estadiaActiva(placa);
            Usuario empleado = usuarios.buscarPorNombreUsuario(usuarioCaja)
                    .orElseThrow(() -> new IllegalStateException(
                            "El usuario de caja '" + usuarioCaja
                                    + "' no existe: revise el script de datos iniciales."));

            estadia.pagarEnCaja(ahora, empleado);
            estadias.actualizar(estadia);
            return aDTO(estadia, ahora);
        });
    }

    @Override
    public List<EstadiaDTO> listarEnParqueadero() {

        return gestor.ejecutar(() -> {
            LocalDateTime ahora = LocalDateTime.now(reloj);

            return estadias.listarActivas().stream().map(estadia -> aDTO(estadia, ahora))
                    .toList();
        });
    }

    private Estadia estadiaActiva(String placa) {

        String placaNormalizada = Vehiculo.normalizarPlaca(placa);

        return estadias.buscarActivaPorPlaca(placaNormalizada)
                .orElseThrow(() -> new NegocioException("No hay una estadía activa para la placa " + placaNormalizada));
    }

    private EstadiaDTO aDTO(Estadia estadia, LocalDateTime ahora) {
        return EstadiaMapper.aDTO(estadia, estadia.calcularValor(ahora, politica));
    }
}
