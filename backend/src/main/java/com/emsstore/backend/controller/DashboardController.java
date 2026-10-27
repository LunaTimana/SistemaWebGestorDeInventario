package com.emsstore.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbc;
    public DashboardController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        Map<String, Object> kpis = jdbc.queryForMap("""
            SELECT COUNT(DISTINCT p.id) AS totalProductos,
                   COALESCE(SUM(v.stock_actual),0) AS totalUnidades,
                   COALESCE(SUM(v.stock_actual * p.precio_venta),0) AS valorComercial,
                   COALESCE(SUM(CASE WHEN v.stock_actual <= v.stock_minimo THEN 1 ELSE 0 END),0) AS alertasStockBajo,
                   (SELECT COUNT(*) FROM movimientos_inventario m WHERE DATE(m.fecha)=CURRENT_DATE()) AS movimientosHoy
            FROM productos p LEFT JOIN variantes_producto v ON v.producto_id=p.id
            """);
        List<Map<String,Object>> categorias = jdbc.queryForList("""
            SELECT c.nombre, COALESCE(SUM(v.stock_actual),0) AS unidades
            FROM categorias c LEFT JOIN productos p ON p.categoria_id=c.id
            LEFT JOIN variantes_producto v ON v.producto_id=p.id
            GROUP BY c.id,c.nombre ORDER BY unidades DESC,c.nombre
            """);
        int total = categorias.stream().mapToInt(r -> ((Number) r.get("unidades")).intValue()).sum();
        for (Map<String,Object> c : categorias) {
            int units = ((Number)c.get("unidades")).intValue();
            c.put("porcentaje", total == 0 ? 0 : Math.round(units * 10000.0 / total) / 100.0);
        }
        List<Map<String,Object>> alertas = jdbc.queryForList("""
            SELECT v.codigo_unico AS sku, p.nombre_modelo AS producto, v.talla, v.stock_actual AS stock,
                   v.stock_minimo AS minimo, GREATEST(v.stock_minimo-v.stock_actual,0) AS deficit
            FROM variantes_producto v JOIN productos p ON p.id=v.producto_id
            WHERE v.stock_actual <= v.stock_minimo ORDER BY (v.stock_minimo-v.stock_actual) DESC, p.nombre_modelo LIMIT 5
            """);
        List<Map<String,Object>> movimientos = jdbc.queryForList("""
            SELECT m.id,m.fecha,m.tipo,m.motivo,m.cantidad,v.codigo_unico AS sku,p.nombre_modelo AS producto,
                   u.nombre AS responsable_nombre,u.apellido AS responsable_apellido,m.documento,m.destino_origen
            FROM movimientos_inventario m JOIN variantes_producto v ON v.id=m.variante_id
            JOIN productos p ON p.id=v.producto_id JOIN usuarios u ON u.id=m.usuario_id
            ORDER BY m.fecha DESC,m.id DESC LIMIT 5
            """);
        movimientos.forEach(m -> m.put("responsable", String.valueOf(m.remove("responsable_nombre")) + " " + String.valueOf(m.remove("responsable_apellido"))));
        Map<String,Object> out = new LinkedHashMap<>(kpis);
        out.put("categorias", categorias); out.put("alertas", alertas); out.put("movimientos", movimientos);
        return out;
    }
}
