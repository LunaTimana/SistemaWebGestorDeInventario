package com.emsstore.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    public UsuarioController(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }

    @GetMapping
    public List<Map<String,Object>> listar() {
        return jdbc.query("""
            SELECT u.id,u.nombre,u.apellido,u.correo,r.nombre AS rol,u.estado,u.ultima_conexion AS ultimaConexion,u.fecha_creacion AS fechaCreacion
            FROM usuarios u JOIN roles r ON r.id=u.rol_id ORDER BY u.nombre,u.apellido
            """, (rs,n) -> {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id",rs.getInt("id")); m.put("nombre",rs.getString("nombre")); m.put("apellido",rs.getString("apellido"));
                m.put("correo",rs.getString("correo")); m.put("rol",rs.getString("rol")); m.put("estado",rs.getString("estado"));
                m.put("ultimaConexion",rs.getTimestamp("ultimaConexion") == null ? null : rs.getTimestamp("ultimaConexion").toString());
                m.put("fechaCreacion",rs.getTimestamp("fechaCreacion") == null ? null : rs.getTimestamp("fechaCreacion").toString());
                return m;
            });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String,Object> crear(@Valid @RequestBody NuevoUsuario req) {
        Integer roleId = roleId(req.rol());
        if (jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo)=LOWER(?)", Integer.class, req.correo().trim()) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya existe un usuario con ese correo.");
        String temp = req.contrasena() == null || req.contrasena().isBlank() ? "EMSStoreTemp2026" : req.contrasena();
        jdbc.update("INSERT INTO usuarios(nombre,apellido,correo,contrasena_hash,debe_cambiar_contrasena,rol_id,estado) VALUES(?,?,?,?,TRUE,?,'activo')",
                req.nombre().trim(),req.apellido().trim(),req.correo().trim().toLowerCase(),encoder.encode(temp),roleId);
        return Map.of("mensaje","Usuario creado. Debe cambiar su contraseña temporal.","contrasenaTemporal",temp);
    }

    @PutMapping("/{id}/rol")
    @Transactional
    public Map<String,Object> cambiarRol(@PathVariable int id, @Valid @RequestBody CambioRol req) {
        if (jdbc.update("UPDATE usuarios SET rol_id=? WHERE id=?",roleId(req.rol()),id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado.");
        return Map.of("mensaje","Rol actualizado correctamente.");
    }

    @PatchMapping("/{id}/estado")
    @Transactional
    public Map<String,Object> cambiarEstado(@PathVariable int id, @Valid @RequestBody CambioEstado req) {
        if (!List.of("activo","inactivo").contains(req.estado())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Estado inválido.");
        if (jdbc.update("UPDATE usuarios SET estado=? WHERE id=?",req.estado(),id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado.");
        return Map.of("mensaje","Estado actualizado correctamente.");
    }

    private Integer roleId(String role) {
        List<Integer> ids = jdbc.query("SELECT id FROM roles WHERE nombre=?",(rs,n)->rs.getInt(1),role);
        if(ids.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rol inválido.");
        return ids.get(0);
    }
    public record NuevoUsuario(@NotBlank String nombre,@NotBlank String apellido,@NotBlank @Email String correo,String contrasena,@NotBlank String rol) {}
    public record CambioRol(@NotBlank String rol) {}
    public record CambioEstado(@NotNull String estado) {}
}
