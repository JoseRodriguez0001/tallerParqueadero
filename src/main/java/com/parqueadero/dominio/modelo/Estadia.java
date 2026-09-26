package com.parqueadero.dominio.modelo;

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
        throw new UnsupportedOperationException("Pendiente (A): RN-01, RN-16");
    }

    public static Estadia reconstruir(Integer id, Vehiculo vehiculo, Tarifa tarifa,
            LocalDateTime fechaIngreso, LocalDateTime fechaSalida,
            BigDecimal valorTotal, EstadoEstadia estado, Pago pago) {
        return new Estadia(id, vehiculo, tarifa, fechaIngreso, fechaSalida, valorTotal, estado, pago);
    }

    public BigDecimal calcularValor(LocalDateTime ahora, PoliticaCobro politica) {
        throw new UnsupportedOperationException("Pendiente ");
    }

    public void registrarSalida(LocalDateTime fecha, PoliticaCobro politica) {
        throw new UnsupportedOperationException("Pendiente ");
    }

    public void pagarEnCaja(LocalDateTime fecha, Usuario empleado) {
        throw new UnsupportedOperationException("Pendiente ");
    }

    public void pagarEnLinea(LocalDateTime fecha, String referencia) {
        throw new UnsupportedOperationException("Pendiente ");
    }

    public boolean estaActiva() {
        throw new UnsupportedOperationException("Pendiente");
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
