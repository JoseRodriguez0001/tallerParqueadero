package com.parqueadero.dominio.modelo;

public class Vehiculo {

    private final Integer id;
    private final String placa;
    private final TipoVehiculo tipoVehiculo;

    public Vehiculo(String placa, TipoVehiculo tipoVehiculo) {
        this(null, placa, tipoVehiculo);
    }

    private Vehiculo(Integer id, String placa, TipoVehiculo tipoVehiculo) {
        this.id = id;
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
    }

    public static Vehiculo reconstruir(Integer id, String placa, TipoVehiculo tipoVehiculo) {
        return new Vehiculo(id, placa, tipoVehiculo);
    }

    // Mayúsculas, sin espacios ni guiones ("abc-123 " → "ABC123").
    // Única definición de la regla: la usan este constructor y los servicios antes de buscar por placa.
    public static String normalizarPlaca(String placa) {
        if (placa == null) {
            return "";
        }
        return placa.replaceAll("[\\s-]", "").toUpperCase();
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Integer getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }
}
