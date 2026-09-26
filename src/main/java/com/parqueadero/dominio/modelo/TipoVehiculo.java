package com.parqueadero.dominio.modelo;

public class TipoVehiculo {

    private final Integer id;
    private final String codigo;
    private final String nombre;
    private boolean activo;

    public TipoVehiculo(String codigo, String nombre) {
        this(null, codigo, nombre, true);
    }

    private TipoVehiculo(Integer id, String codigo, String nombre, boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = activo;
    }

    public static TipoVehiculo reconstruir(Integer id, String codigo, String nombre, boolean activo) {
        return new TipoVehiculo(id, codigo, nombre, activo);
    }

    public boolean estaActivo() {
        return activo;
    }

    public void desactivar() {
        throw new UnsupportedOperationException("Pendiente ");
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Integer getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}
