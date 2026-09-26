package com.parqueadero.dominio.modelo;

public class Usuario {

    private final Integer id;
    private final String nombreUsuario;
    private final String nombre;
    private final String contrasenaHash;
    private final Rol rol;
    private boolean activo;

    private Usuario(Integer id, String nombreUsuario, String nombre,
            String contrasenaHash, Rol rol, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = activo;
    }

    public static Usuario reconstruir(Integer id, String nombreUsuario, String nombre,
            String contrasenaHash, Rol rol, boolean activo) {
        return new Usuario(id, nombreUsuario, nombre, contrasenaHash, rol, activo);
    }

    public boolean estaActivo() {
        return activo;
    }

    public void desactivar() {
        throw new UnsupportedOperationException("Pendiente ");
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Integer getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Rol getRol() {
        return rol;
    }
}
