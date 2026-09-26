package com.parqueadero.presentacion.controlador;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class ControladorBase implements HttpHandler {

    // Llama a procesar(); si lanza NegocioException, muestra el mensaje en la página.
    @Override
    public final void handle(HttpExchange intercambio) throws IOException {
        throw new UnsupportedOperationException("Pendiente (B): base web");
    }

    // Cada controlador atiende aquí sus rutas (GET y POST).
    protected abstract void procesar(HttpExchange intercambio) throws IOException;

    // Cuerpo application/x-www-form-urlencoded, decodificado en UTF-8
    protected Map<String, String> leerFormulario(HttpExchange intercambio) throws IOException {
        throw new UnsupportedOperationException("Pendiente (B): base web");
    }

    protected void responderHtml(HttpExchange intercambio, String html) throws IOException {
        throw new UnsupportedOperationException("Pendiente (B): base web");
    }

    // Patrón POST-redirect-GET: después de un POST exitoso se redirige a una página GET
    protected void redirigir(HttpExchange intercambio, String ruta) throws IOException {
        throw new UnsupportedOperationException("Pendiente (B): base web");
    }
}
