package com.parqueadero.presentacion.vista;

public final class Vista {

    private Vista() {
    }

    // Layout común: encabezado, menú y estilos
    public static String pagina(String titulo, String contenido) {
        throw new UnsupportedOperationException(" (B): base web");
    }

    // Obligatorio en todo dato que se muestre en el HTML (evita inyección de
    // HTML/scripts)
    public static String escapar(String texto) {
        throw new UnsupportedOperationException(" base web");
    }
}
