package com.parqueadero.comun;

/**
 * Violación de una regla de negocio.
 *
 * La lanzan el dominio y los servicios; la presentación la captura para mostrar
 * el mensaje al usuario. Cuando corresponde a una regla del documento de diseño,
 * lleva su código (por ejemplo "RN-10") para poder rastrearla.
 */
public class NegocioException extends RuntimeException {

    private final String regla;

    /** Violación de una regla identificada: el mensaje queda como "RN-10: ...". */
    public NegocioException(String regla, String mensaje) {
        super(regla + ": " + mensaje);
        this.regla = regla;
    }

    /** Violación sin una regla específica (por ejemplo, un dato no encontrado). */
    public NegocioException(String mensaje) {
        super(mensaje);
        this.regla = null;
    }

    /** Código de la regla violada, o null si no corresponde a una regla específica. */
    public String getRegla() {
        return regla;
    }
}
