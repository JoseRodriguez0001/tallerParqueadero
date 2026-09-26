package com.parqueadero.datos.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    private static final String ARCHIVO = "db.properties";

    private final String url;
    private final String usuario;
    private final String contrasena;

    public ConexionBD() {
        Properties propiedades = cargarPropiedades();
        this.url = requerida(propiedades, "db.url");
        this.usuario = requerida(propiedades, "db.usuario");
        this.contrasena = requerida(propiedades, "db.contrasena");
    }

    // Abre una conexión nueva. Quien la abre es responsable de cerrarla.
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    // helpers
    private static Properties cargarPropiedades() {
        try (InputStream entrada = abrirArchivo()) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontró " + ARCHIVO + " ni junto a la aplicación ni en src/main/resources. "
                                + "Cópialo desde db.properties.example y completa tus datos.");
            }
            Properties propiedades = new Properties();
            propiedades.load(entrada);
            return propiedades;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO + ".", e);
        }
    }

    // El archivo externo tiene prioridad sobre el empaquetado; null si no hay
    // ninguno
    private static InputStream abrirArchivo() throws IOException {
        Path externo = Path.of(ARCHIVO);
        if (Files.isRegularFile(externo)) {
            return Files.newInputStream(externo);
        }
        return ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO);
    }

    private static String requerida(Properties propiedades, String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la propiedad '" + clave + "' en " + ARCHIVO + ".");
        }
        return valor.trim();
    }
}
