package com.emsstore.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("estado", "ok");
        result.put("baseDatos", jdbc.queryForObject("SELECT DATABASE()", String.class));
        result.put("horaServidor", Instant.now().toString());
        result.put("tablas", jdbc.queryForList("""
            SELECT table_name AS nombre FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name IN ('roles','usuarios','marcas','categorias','proveedores','productos','variantes_producto','movimientos_inventario')
            ORDER BY table_name
            """, String.class));
        return result;
    }
}
