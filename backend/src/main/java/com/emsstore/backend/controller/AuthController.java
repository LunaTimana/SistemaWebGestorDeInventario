package com.emsstore.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.emsstore.backend.security.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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


    @GetMapping("/me")
    public Map<String,Object> miCuenta(Authentication auth) {
        Integer id = (Integer) auth.getPrincipal();
        List<Map<String,Object>> rows = jdbcTemplate.queryForList("SELECT u.id,u.nombre,u.apellido,u.correo,u.debe_cambiar_contrasena,r.nombre AS rol FROM usuarios u JOIN roles r ON r.id=u.rol_id WHERE u.id=? AND u.estado='activo'", id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"La cuenta no está activa.");
        return rows.get(0);
    }

    @PutMapping("/me")
    public Map<String,Object> actualizarCuenta(@Valid @RequestBody DatosCuenta datos, Authentication auth) {
        Integer id = (Integer) auth.getPrincipal();
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo)=LOWER(?) AND id<>?", Integer.class, datos.correo().trim(), id) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Ese correo ya está registrado por otro usuario.");
        jdbcTemplate.update("UPDATE usuarios SET nombre=?,apellido=?,correo=? WHERE id=? AND estado='activo'", datos.nombre().trim(), datos.apellido().trim(), datos.correo().trim().toLowerCase(), id);
        return miCuenta(auth);
    }

    @PutMapping("/me/contrasena")
    public Map<String,Object> cambiarContrasena(@Valid @RequestBody CambioContrasena datos, Authentication auth) {
        Integer id = (Integer) auth.getPrincipal();
        List<String> hashes = jdbcTemplate.query("SELECT contrasena_hash FROM usuarios WHERE id=? AND estado='activo'", (rs,n)->rs.getString(1), id);
        if (hashes.isEmpty() || !passwordEncoder.matches(datos.actual(), hashes.get(0)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La contraseña actual no es correcta.");
        if (!datos.nueva().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número.");
        jdbcTemplate.update("UPDATE usuarios SET contrasena_hash=?,debe_cambiar_contrasena=FALSE WHERE id=?", passwordEncoder.encode(datos.nueva()), id);
        return Map.of("mensaje","Contraseña actualizada correctamente.");
    }

    public record DatosCuenta(@NotBlank String nombre, @NotBlank String apellido, @NotBlank @Email String correo) {}
    public record CambioContrasena(@NotBlank String actual, @NotBlank String nueva) {}

    public record LoginRequest(String correo, String contrasena) {}
    private record UsuarioConHash(Integer id, String nombre, String apellido, String correo,
            String hash, boolean debeCambiarContrasena, String rol) {}
}
