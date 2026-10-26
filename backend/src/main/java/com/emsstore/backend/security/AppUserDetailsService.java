package com.emsstore.backend.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.emsstore.backend.repository.UsuarioRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public AppUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        var usuario = usuarios.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
        String autoridad = "ROLE_" + rolClave(usuario.getRol().getNombre());
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .authorities(autoridad)
                .disabled(usuario.getEstado() != com.emsstore.backend.model.EstadoUsuario.activo)
                .build();
    }

    public static String rolClave(String rol) {
        return switch (rol.toLowerCase(java.util.Locale.ROOT)) {
            case "administrador", "admin" -> "ADMINISTRADOR";
            case "almacén", "almacen" -> "ALMACEN";
            case "ventas" -> "VENTAS";
            default -> "SIN_ROL";
        };
    }
}
