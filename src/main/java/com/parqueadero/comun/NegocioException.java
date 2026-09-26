package com.parqueadero.comun;

/**
 * Violación de una regla de negocio.
 *
 * La lanzan el dominio y los servicios; la presentación la captura para mostrar
 * el mensaje al usuario.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
