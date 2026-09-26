package com.parqueadero.datos.transaccion;

import java.util.function.Supplier;

/**
 * Ejecuta una operación como una sola transacción.
 *
 * La capa de negocio decide el alcance: todo lo que ocurre dentro de la operación
 * se confirma junto o se deshace junto. Todo método de servicio, incluso los de
 * solo lectura, se ejecuta a través de este gestor.
 */
public interface GestorTransacciones {

    /**
     * Ejecuta la operación dentro de una transacción.
     * Confirma los cambios si termina correctamente y los deshace si lanza una excepción,
     * que se propaga sin modificarse (por ejemplo, una NegocioException).
     */
    <T> T ejecutar(Supplier<T> operacion);
}
