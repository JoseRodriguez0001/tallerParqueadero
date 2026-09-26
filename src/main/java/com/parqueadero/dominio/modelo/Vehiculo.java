package com.parqueadero.dominio.modelo;

import java.util.regex.Pattern;

import com.parqueadero.comun.NegocioException;

public class Vehiculo {

    private static final Pattern PLACA_VALIDA = Pattern.compile("[A-Z0-9]{1,10}");

    private final Integer id;
    private final String placa;
    private final TipoVehiculo tipoVehiculo;

    public Vehiculo(String placa, TipoVehiculo tipoVehiculo) {
        this(null, validarPlaca(placa), validarTipo(tipoVehiculo));
    }

    private Vehiculo(Integer id, String placa, TipoVehiculo tipoVehiculo) {
        this.id = id;
        this.placa = placa;
        this.tipoVehiculo = tipoVehiculo;
    }

    public static Vehiculo reconstruir(Integer id, String placa, TipoVehiculo tipoVehiculo) {
        return new Vehiculo(id, placa, tipoVehiculo);
    }

    public static String normalizarPlaca(String placa) {
        if (placa == null) {
            return "";
        }
        return placa.replaceAll("[\\s-]", "").toUpperCase();
    }

    private static String validarPlaca(String placa) {
        String normalizada = normalizarPlaca(placa);
        if (normalizada.isEmpty()) {
            throw new NegocioException("Ingrese la placa del vehículo.");
        }
        if (!PLACA_VALIDA.matcher(normalizada).matches()) {
            throw new NegocioException("La placa " + placa
                    + " no es válida: use solo letras y números, máximo 10 caracteres.");
        }
        return normalizada;
    }

    private static TipoVehiculo validarTipo(TipoVehiculo tipo) {
        if (tipo == null) {
            throw new NegocioException("Seleccione el tipo de vehículo.");
        }
        if (!tipo.estaActivo()) {
            throw new NegocioException("El tipo de vehículo " + tipo.getNombre() + " no está disponible.");
        }
        return tipo;
    }

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
