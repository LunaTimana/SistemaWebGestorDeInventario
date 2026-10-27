package com.emsstore.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movimientos")
@Validated
public class MovimientoInventarioController {
    private final JdbcTemplate jdbc;
    public MovimientoInventarioController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public List<Map<String, Object>> listar(@RequestParam(required = false) String tipo) {
        String sql = "SELECT m.id, m.fecha, m.tipo, m.motivo, m.cantidad, m.documento, m.destino_origen, m.comentario, " +
                "v.codigo_unico AS sku, v.talla, v.color, p.nombre_modelo AS producto, u.nombre AS responsable_nombre, " +
                "u.apellido AS responsable_apellido, pr.nombre AS proveedor " +
                "FROM movimientos_inventario m JOIN variantes_producto v ON v.id=m.variante_id " +
                "JOIN productos p ON p.id=v.producto_id JOIN usuarios u ON u.id=m.usuario_id " +
                "LEFT JOIN proveedores pr ON pr.id=m.proveedor_id " +
                (tipo == null || tipo.isBlank() ? "" : "WHERE m.tipo = ? ") + "ORDER BY m.fecha DESC, m.id DESC";
        return tipo == null || tipo.isBlank()
                ? jdbc.query(sql, this::filaMovimiento)
                : jdbc.query(sql, this::filaMovimiento, tipo.trim().toLowerCase());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> registrar(@Valid @RequestBody MovimientoRequest request, Authentication auth) {
        String tipo = request.tipo().trim().toLowerCase();
        String motivo = request.motivo().trim().toLowerCase();
        validarTipoMotivo(tipo, motivo);
        Integer userId = (Integer) auth.getPrincipal();
        List<StockRow> rows = jdbc.query("SELECT stock_actual, stock_minimo FROM variantes_producto WHERE id=? FOR UPDATE",
                (rs, n) -> new StockRow(rs.getInt("stock_actual"), rs.getInt("stock_minimo")), request.varianteId());
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La variante/SKU no existe.");
        StockRow stock = rows.get(0);
        int nuevoStock = tipo.equals("entrada") ? stock.actual() + request.cantidad() : stock.actual() - request.cantidad();
        if (nuevoStock < 0) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Stock insuficiente. Disponible: " + stock.actual() + " unidades.");
        Integer proveedorId = request.proveedorId();
        if (tipo.equals("salida")) proveedorId = null;
        if (tipo.equals("entrada") && motivo.equals("compra") && proveedorId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un proveedor para registrar una compra.");
        jdbc.update("UPDATE variantes_producto SET stock_actual=? WHERE id=?", nuevoStock, request.varianteId());
        jdbc.update("INSERT INTO movimientos_inventario (variante_id,tipo,motivo,cantidad,usuario_id,proveedor_id,documento,destino_origen,comentario) VALUES (?,?,?,?,?,?,?,?,?)",
                request.varianteId(), tipo, motivo, request.cantidad(), userId, proveedorId,
                blankToNull(request.documento()), blankToNull(request.destinoOrigen()), blankToNull(request.comentario()));
        return Map.of("mensaje", "Movimiento registrado y stock actualizado.", "stockAnterior", stock.actual(),
                "stockNuevo", nuevoStock, "varianteId", request.varianteId());
    }

    private void validarTipoMotivo(String tipo, String motivo) {
        boolean ok = (tipo.equals("entrada") && List.of("compra", "ajuste").contains(motivo)) ||
                (tipo.equals("salida") && List.of("venta", "devolucion_proveedor", "merma", "ajuste").contains(motivo));
        if (!ok) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo/motivo inválido según las reglas de inventario.");
    }

    private Map<String, Object> filaMovimiento(ResultSet rs, int row) throws SQLException {
        return Map.ofEntries(Map.entry("id", rs.getInt("id")), Map.entry("fecha", rs.getTimestamp("fecha").toString()),
                Map.entry("tipo", rs.getString("tipo")), Map.entry("motivo", rs.getString("motivo")),
                Map.entry("cantidad", rs.getInt("cantidad")), Map.entry("documento", value(rs.getString("documento"))),
                Map.entry("destinoOrigen", value(rs.getString("destino_origen"))), Map.entry("comentario", value(rs.getString("comentario"))),
                Map.entry("sku", rs.getString("sku")), Map.entry("talla", rs.getString("talla")), Map.entry("color", rs.getString("color")),
                Map.entry("producto", rs.getString("producto")), Map.entry("responsable", rs.getString("responsable_nombre") + " " + rs.getString("responsable_apellido")),
                Map.entry("proveedor", value(rs.getString("proveedor"))));
    }
    private static String value(String s) { return s == null ? "" : s; }
    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    public record MovimientoRequest(@NotNull Integer varianteId, @NotBlank String tipo, @NotBlank String motivo,
            @Min(1) int cantidad, Integer proveedorId, String documento, String destinoOrigen, String comentario) {}
    private record StockRow(int actual, int minimo) {}
}
