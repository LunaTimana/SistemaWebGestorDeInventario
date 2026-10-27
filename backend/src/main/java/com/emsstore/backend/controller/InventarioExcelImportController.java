package com.emsstore.backend.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/inventario")
public class InventarioExcelImportController {
    private final JdbcTemplate jdbc;
    public InventarioExcelImportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Imports stock by existing SKU; it never creates unknown products or variants. */
    @PostMapping(value = "/importar/excel", consumes = "multipart/form-data")
    @Transactional
    public Map<String, Object> importar(@RequestPart("archivo") MultipartFile archivo, Authentication auth) {
        if (archivo == null || archivo.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo Excel.");
        String name = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".xlsx")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe tener formato .xlsx.");
        if (archivo.getSize() > 10 * 1024 * 1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo supera el límite de 10 MB.");
        int userId = (Integer) auth.getPrincipal();
        int actualizados = 0, movimientos = 0, omitidos = 0;
        List<String> errores = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El Excel no contiene filas de inventario.");
            Row header = sheet.getRow(0);
            Map<String, Integer> columns = new HashMap<>();
            DataFormatter formatter = new DataFormatter();
            for (Cell c : header) columns.put(formatter.formatCellValue(c).trim().toLowerCase(Locale.ROOT), c.getColumnIndex());
            Integer skuCol = columns.get("sku"), stockCol = columns.get("stock actual"), minCol = columns.get("stock mínimo");
            if (skuCol == null || stockCol == null || minCol == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan columnas requeridas: SKU, Stock actual y Stock mínimo.");
            Set<String> skusProcesados = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) { omitidos++; continue; }
                String sku = formatter.formatCellValue(row.getCell(skuCol)).trim();
                if (sku.isBlank()) { omitidos++; continue; }
                if (!skusProcesados.add(sku)) { errores.add("Fila " + (i + 1) + ": SKU duplicado en el archivo: " + sku); continue; }
                try {
                    int stock = leerEntero(row.getCell(stockCol), formatter);
                    int minimo = leerEntero(row.getCell(minCol), formatter);
                    if (stock < 0 || minimo < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
                    List<Stock> found = jdbc.query("SELECT id, stock_actual FROM variantes_producto WHERE codigo_unico=? FOR UPDATE",
                            (rs, n) -> new Stock(rs.getInt("id"), rs.getInt("stock_actual")), sku);
                    if (found.isEmpty()) { errores.add("Fila " + (i + 1) + ": SKU no encontrado: " + sku); continue; }
                    Stock old = found.get(0);
                    jdbc.update("UPDATE variantes_producto SET stock_actual=?, stock_minimo=? WHERE id=?", stock, minimo, old.id());
                    actualizados++;
                    if (old.actual() != stock) {
                        String tipo = stock > old.actual() ? "entrada" : "salida";
                        int cantidad = Math.abs(stock - old.actual());
                        jdbc.update("INSERT INTO movimientos_inventario (variante_id,tipo,motivo,cantidad,usuario_id,documento,comentario) VALUES (?,?,?,?,?,?,?)",
                                old.id(), tipo, "ajuste", cantidad, userId, "Importación Excel", "Ajuste de stock mediante importación Excel");
                        movimientos++;
                    }
                } catch (IllegalArgumentException ex) {
                    errores.add("Fila " + (i + 1) + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el Excel. Verifica que sea un .xlsx válido.");
        }
        return Map.of("mensaje", "Importación procesada.", "filasActualizadas", actualizados,
                "movimientosRegistrados", movimientos, "filasOmitidas", omitidos, "errores", errores);
    }

    private int leerEntero(Cell cell, DataFormatter formatter) {
        if (cell == null) throw new IllegalArgumentException("Falta un valor de stock.");
        String raw = formatter.formatCellValue(cell).trim();
        try {
            double number = Double.parseDouble(raw.replace(",", "."));
            if (number < 0 || number != Math.floor(number) || number > Integer.MAX_VALUE)
                throw new IllegalArgumentException("El stock debe ser un entero mayor o igual a cero.");
            return (int) number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El stock debe ser un número entero.");
        }
    }
    private record Stock(int id, int actual) {}
}
