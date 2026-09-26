package com.parqueadero.dominio.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Pago {

    private final Integer id;
    private final BigDecimal valor;
    private final LocalDateTime fechaPago;
    private final CanalPago canal;
    private final String referencia;
    private final Usuario empleado;

    private Pago(Integer id, BigDecimal valor, LocalDateTime fechaPago,
            CanalPago canal, String referencia, Usuario empleado) {
        this.id = id;
        this.valor = valor;
        this.fechaPago = fechaPago;
        this.canal = canal;
        this.referencia = referencia;
        this.empleado = empleado;
    }

    static Pago enCaja(BigDecimal valor, LocalDateTime fechaPago, Usuario empleado) {
        Objects.requireNonNull(empleado, "El pago en caja requiere el empleado que lo recibió ");
        return new Pago(null, valor, fechaPago, CanalPago.CAJA, null, empleado);
    }

    public static Pago reconstruir(Integer id, BigDecimal valor, LocalDateTime fechaPago,
            CanalPago canal, String referencia, Usuario empleado) {
        return new Pago(id, valor, fechaPago, canal, referencia, empleado);
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public CanalPago getCanal() {
        return canal;
    }

    public Optional<String> getReferencia() {
        return Optional.ofNullable(referencia);
    }

    public Optional<Usuario> getEmpleado() {
        return Optional.ofNullable(empleado);
    }
}
