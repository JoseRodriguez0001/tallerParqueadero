package com.parqueadero.presentacion.controlador;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.parqueadero.comun.NegocioException;

// Ejecuta las acciones de los botones con un manejo de errores
final class Acciones {

    private static final Logger LOG = Logger.getLogger(Acciones.class.getName());

    static final String MENSAJE_ERROR_INESPERADO = "Ocurrió un error inesperado. Intente de nuevo y, si persiste, contacte al administrador.";

    private Acciones() {
    }

    static void ejecutar(Runnable accion, Consumer<String> mostrarError) {
        try {
            accion.run();
        } catch (NegocioException e) {
            mostrarError.accept(e.getMessage());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado al ejecutar una acción de la interfaz", e);
            mostrarError.accept(MENSAJE_ERROR_INESPERADO);
        }
    }

    static String obligatorio(String valor, String mensajeSiFalta) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException(mensajeSiFalta);
        }
        return valor;
    }
}
