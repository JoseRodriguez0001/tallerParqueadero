package com.parqueadero.datos.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones a PostgreSQL con los datos de {@code db.properties}.
 *
 * El archivo se lee del classpath ({@code src/main/resources/db.properties}) y no se
 * sube al repositorio porque contiene la contraseña. La plantilla es
 * {@code db.properties.example}.
 *
 * Solo la usa {@link GestorTransaccionesJdbc}: los repositorios nunca abren conexiones.
 */
public class ConexionBD {

    private static final String ARCHIVO = "db.properties";

    private final String url;
    private final String usuario;
    private final String contrasena;

    public ConexionBD() {
        Properties propiedades = cargarPropiedades();
        this.url        = requerida(propiedades, "db.url");
        this.usuario    = requerida(propiedades, "db.usuario");
        this.contrasena = requerida(propiedades, "db.contrasena");
    }

    /** Abre una conexión nueva. Quien la abre es responsable de cerrarla. */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    private static Properties cargarPropiedades() {
        try (InputStream entrada = ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró " + ARCHIVO + " en src/main/resources. "
                        + "Cópialo desde db.properties.example y completa tus datos.");
            }
            Properties propiedades = new Properties();
            propiedades.load(entrada);
            return propiedades;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO + ".", e);
        }
    }

    private static String requerida(Properties propiedades, String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la propiedad '" + clave + "' en " + ARCHIVO + ".");
        }
        return valor.trim();
    }
}
