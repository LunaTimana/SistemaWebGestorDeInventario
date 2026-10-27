package com.emsstore.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.emsstore.backend.security.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public Map<String, Object> iniciarSesion(@RequestBody LoginRequest request) {
        if (request == null || request.correo() == null || request.contrasena() == null
                || request.correo().isBlank() || request.contrasena().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes ingresar correo y contraseña.");
        }

        String sql = "SELECT u.id, u.nombre, u.apellido, u.correo, u.contrasena_hash, "
                + "u.debe_cambiar_contrasena, r.nombre AS rol "
                + "FROM usuarios u INNER JOIN roles r ON r.id = u.rol_id "
                + "WHERE LOWER(u.correo) = LOWER(?) AND u.estado = 'activo'";
        List<UsuarioConHash> resultados = jdbcTemplate.query(sql, (rs, rowNum) -> new UsuarioConHash(
                rs.getInt("id"), rs.getString("nombre"), rs.getString("apellido"),
                rs.getString("correo"), rs.getString("contrasena_hash"),
                rs.getBoolean("debe_cambiar_contrasena"), rs.getString("rol")
        ), request.correo().trim());

        if (resultados.isEmpty() || resultados.get(0).hash() == null
                || !passwordEncoder.matches(request.contrasena(), resultados.get(0).hash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrectos, o usuario inactivo.");
        }

        UsuarioConHash usuario = resultados.get(0);
        jdbcTemplate.update("UPDATE usuarios SET ultima_conexion = CURRENT_TIMESTAMP WHERE id = ?", usuario.id());
        Map<String, Object> datosUsuario = Map.of(
                "id", usuario.id(), "nombre", usuario.nombre(), "apellido", usuario.apellido(),
                "correo", usuario.correo(), "debeCambiarContrasena", usuario.debeCambiarContrasena(),
                "rol", usuario.rol());
        return Map.of("mensaje", "Inicio de sesión correcto.", "usuario", datosUsuario,
                "token", tokenService.crearToken(usuario.id(), usuario.rol()));
    }

    public record LoginRequest(String correo, String contrasena) {}
    private record UsuarioConHash(Integer id, String nombre, String apellido, String correo,
            String hash, boolean debeCambiarContrasena, String rol) {}
}
