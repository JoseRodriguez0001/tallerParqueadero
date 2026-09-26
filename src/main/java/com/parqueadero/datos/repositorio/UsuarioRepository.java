package com.parqueadero.datos.repositorio;

import java.util.Optional;

import com.parqueadero.dominio.modelo.Usuario;

public interface UsuarioRepository {
    Optional<Usuario> buscarPorNombreUsuario(String nombre);
}
