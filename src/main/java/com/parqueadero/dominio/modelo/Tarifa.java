package com.parqueadero.dominio.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class Tarifa {

    private final Integer id;
    private final TipoVehiculo tipoVehiculo;
    private final BigDecimal valorHora;
    private final LocalDateTime vigenteDesde;
    private final LocalDateTime vigenteHasta;

    public Tarifa(TipoVehiculo tipoVehiculo, BigDecimal valorHora, LocalDateTime vigenteDesde) {
        this(null, tipoVehiculo, valorHora, vigenteDesde, null);
    }

    private Tarifa(Integer id, TipoVehiculo tipoVehiculo, BigDecimal valorHora,
            LocalDateTime vigenteDesde, LocalDateTime vigenteHasta) {
        this.id = id;
        this.tipoVehiculo = tipoVehiculo;
        this.valorHora = valorHora;
        this.vigenteDesde = vigenteDesde;
        this.vigenteHasta = vigenteHasta;
    }

    public static Tarifa reconstruir(Integer id, TipoVehiculo tipoVehiculo, BigDecimal valorHora,
            LocalDateTime vigenteDesde, LocalDateTime vigenteHasta) {
        return new Tarifa(id, tipoVehiculo, valorHora, vigenteDesde, vigenteHasta);
    }

    public boolean estaVigenteEn(LocalDateTime fecha) {
        boolean yaInicio = !fecha.isBefore(vigenteDesde);
        boolean noHaTerminado = vigenteHasta == null || fecha.isBefore(vigenteHasta);
        return yaInicio && noHaTerminado;
    }

    public Integer getId() {
        return id;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public BigDecimal getValorHora() {
        return valorHora;
    }

    public LocalDateTime getVigenteDesde() {
        return vigenteDesde;
    }

    public Optional<LocalDateTime> getVigenteHasta() {
        return Optional.ofNullable(vigenteHasta);
    }
}
