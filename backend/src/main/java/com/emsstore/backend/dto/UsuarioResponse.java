package com.emsstore.backend.dto;

import java.time.LocalDateTime;

import com.emsstore.backend.model.Usuario;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String apellido,
        String correo,
        String rol,
        String estado,
        LocalDateTime ultimaConexion) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getRol().getNombre(),
                usuario.getEstado().name(),
                usuario.getUltimaConexion());
    }
}