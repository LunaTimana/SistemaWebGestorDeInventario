package com.emsstore.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {
    private final JdbcTemplate jdbc;
    public AlertaController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public Map<String,Object> listar() {
        List<Map<String,Object>> items = jdbc.queryForList("""
            SELECT v.id AS varianteId,v.codigo_unico AS sku,p.nombre_modelo AS producto,v.talla,v.color,
                   v.stock_actual AS stock,v.stock_minimo AS minimo,
                   GREATEST(v.stock_minimo-v.stock_actual,0) AS deficit,
                   CASE WHEN v.stock_actual=0 THEN 'CRÍTICO' ELSE 'RIESGO' END AS estado,
                   (SELECT DATE_FORMAT(MAX(m.fecha),'%Y-%m-%dT%H:%i:%s') FROM movimientos_inventario m WHERE m.variante_id=v.id AND m.tipo='entrada') AS ultimoIngreso
            FROM variantes_producto v JOIN productos p ON p.id=v.producto_id
            WHERE v.stock_actual <= v.stock_minimo
            ORDER BY (v.stock_actual=0) DESC, (v.stock_minimo-v.stock_actual) DESC, p.nombre_modelo
            """);
        long criticos = items.stream().filter(i -> "CRÍTICO".equals(i.get("estado"))).count();
        long riesgo = items.size() - criticos;
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("criticos", criticos); result.put("riesgo", riesgo); result.put("total", items.size()); result.put("alertas", items);
        return result;
    }
}
