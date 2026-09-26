package com.parqueadero.dominio.modelo;

import com.parqueadero.comun.NegocioException;
import com.parqueadero.dominio.cobro.PoliticaCobro;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class Estadia {

    private final Integer id;
    private final Vehiculo vehiculo;
    private final Tarifa tarifa;
    private final LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private BigDecimal valorTotal;
    private EstadoEstadia estado;
    private Pago pago;

    private Estadia(Integer id, Vehiculo vehiculo, Tarifa tarifa, LocalDateTime fechaIngreso,
            LocalDateTime fechaSalida, BigDecimal valorTotal, EstadoEstadia estado, Pago pago) {
        this.id = id;
        this.vehiculo = vehiculo;
        this.tarifa = tarifa;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.valorTotal = valorTotal;
        this.estado = estado;
        this.pago = pago;
    }

    public static Estadia iniciar(Vehiculo vehiculo, Tarifa tarifa, LocalDateTime fechaIngreso) {

        TipoVehiculo tipo = vehiculo.getTipoVehiculo();

        if (!tipo.estaActivo()) {
            throw new NegocioException(
                    "El tipo de vehículo " + tipo.getNombre() + " está inactivo y no admite ingresos.");
        }
        if (!tarifa.getTipoVehiculo().getId().equals(tipo.getId())) {
            throw new NegocioException("La tarifa no corresponde al tipo de vehículo " + tipo.getNombre() + ".");
        }
        if (!tarifa.estaVigenteEn(fechaIngreso)) {
            throw new NegocioException("La tarifa no está vigente en la fecha de ingreso.");
        }

        return new Estadia(null, vehiculo, tarifa, fechaIngreso, null, null, EstadoEstadia.DENTRO, null);
    }

    public static Estadia reconstruir(Integer id, Vehiculo vehiculo, Tarifa tarifa,
            LocalDateTime fechaIngreso, LocalDateTime fechaSalida,
            BigDecimal valorTotal, EstadoEstadia estado, Pago pago) {
        return new Estadia(id, vehiculo, tarifa, fechaIngreso, fechaSalida, valorTotal, estado, pago);
    }

    public BigDecimal calcularValor(LocalDateTime ahora, PoliticaCobro politica) {
        if (estado == EstadoEstadia.DENTRO) {
            return politica.calcular(tarifa, fechaIngreso, ahora);
        }

        return valorTotal;
    }

    public void registrarSalida(LocalDateTime fecha, PoliticaCobro politica) {

        if (estado != EstadoEstadia.DENTRO) {
            throw new NegocioException("La estadía no está dentro del parqueadero: su salida ya fue registrada.");
        }

        BigDecimal valor = politica.calcular(tarifa, fechaIngreso, fecha);

        fechaSalida = fecha;
        valorTotal = valor;
        estado = EstadoEstadia.PENDIENTE_PAGO;
    }

    public void pagarEnCaja(LocalDateTime fecha, Usuario empleado) {
        validarPendienteDePago();
        if (empleado != null && !empleado.estaActivo()) {
            throw new NegocioException(
                    "El usuario " + empleado.getNombreUsuario() + " está inactivo y no puede registrar pagos.");
        }

        cerrarCon(Pago.enCaja(valorTotal, fecha, empleado));
    }

    public void pagarEnLinea(LocalDateTime fecha, String referencia) {
        validarPendienteDePago();

        cerrarCon(Pago.enLinea(valorTotal, fecha, referencia));
    }

    private void validarPendienteDePago() {
        if (estado != EstadoEstadia.PENDIENTE_PAGO) {
            String motivo = estado == EstadoEstadia.DENTRO
                    ? "primero se debe registrar la salida"
                    : "la estadía ya fue pagada";
            throw new NegocioException("No se puede registrar el pago: " + motivo + ".");
        }
    }

    private void cerrarCon(Pago nuevoPago) {
        pago = nuevoPago;
        estado = EstadoEstadia.CERRADA;
    }

    public boolean estaActiva() {
        return estado != EstadoEstadia.CERRADA;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public Integer getId() {
        return id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public EstadoEstadia getEstado() {
        return estado;
    }

    public Optional<LocalDateTime> getFechaSalida() {
        return Optional.ofNullable(fechaSalida);
    }

    public Optional<BigDecimal> getValorTotal() {
        return Optional.ofNullable(valorTotal);
    }

    public Optional<Pago> getPago() {
        return Optional.ofNullable(pago);
    }
}
