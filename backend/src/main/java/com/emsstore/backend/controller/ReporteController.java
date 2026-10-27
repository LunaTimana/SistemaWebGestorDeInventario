package com.emsstore.backend.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private static final String MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final JdbcTemplate jdbc;
    public ReporteController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/stock-categoria")
    public List<Map<String,Object>> stockPorCategoria() {
        return jdbc.queryForList("""
            SELECT c.id AS categoriaId,c.nombre AS categoria,COUNT(DISTINCT p.id) AS productos,
                   COALESCE(SUM(v.stock_actual),0) AS unidades,
                   COALESCE(SUM(v.stock_actual*p.precio_venta),0) AS valorVenta,
                   COALESCE(SUM(v.stock_actual*p.costo_unitario),0) AS valorCosto,
                   CASE WHEN SUM(v.stock_actual)=0 THEN 0 ELSE SUM(v.stock_actual*p.precio_venta)/SUM(v.stock_actual) END AS precioPromedio
            FROM categorias c LEFT JOIN productos p ON p.categoria_id=c.id
            LEFT JOIN variantes_producto v ON v.producto_id=p.id
            GROUP BY c.id,c.nombre ORDER BY c.nombre
            """);
    }

    @GetMapping(value="/movimientos/excel", produces=MIME)
    public ResponseEntity<byte[]> exportarMovimientos() {
        List<Map<String,Object>> rows = jdbc.queryForList("""
            SELECT m.fecha,m.tipo,m.motivo,v.codigo_unico AS sku,p.nombre_modelo AS producto,v.talla,v.color,
                   m.cantidad,u.nombre AS responsable_nombre,u.apellido AS responsable_apellido,m.documento,
                   m.destino_origen,m.comentario,pr.nombre AS proveedor
            FROM movimientos_inventario m JOIN variantes_producto v ON v.id=m.variante_id
            JOIN productos p ON p.id=v.producto_id JOIN usuarios u ON u.id=m.usuario_id
            LEFT JOIN proveedores pr ON pr.id=m.proveedor_id ORDER BY m.fecha DESC,m.id DESC
            """);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Movimientos");
            String[] headers = {"Fecha","Tipo","Motivo","SKU","Producto","Talla","Color","Cantidad","Responsable","Documento","Origen/Destino","Comentario","Proveedor"};
            Row header = sheet.createRow(0); Font font = wb.createFont(); font.setBold(true); font.setColor(IndexedColors.WHITE.getIndex());
            CellStyle style = wb.createCellStyle(); style.setFont(font); style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i=0;i<headers.length;i++) { Cell c=header.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(style); }
            int n=1;
            for (Map<String,Object> data : rows) {
                Row r=sheet.createRow(n++);
                Object[] vals = {data.get("fecha"),data.get("tipo"),data.get("motivo"),data.get("sku"),data.get("producto"),data.get("talla"),data.get("color"),data.get("cantidad"),String.valueOf(data.get("responsable_nombre"))+" "+String.valueOf(data.get("responsable_apellido")),data.get("documento"),data.get("destino_origen"),data.get("comentario"),data.get("proveedor")};
                for(int i=0;i<vals.length;i++){Cell c=r.createCell(i); Object v=vals[i]; if(v instanceof Number num)c.setCellValue(num.doubleValue()); else c.setCellValue(v==null?"":v.toString());}
            }
            sheet.createFreezePane(0,1); sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0,rows.size()),0,headers.length-1));
            for(int i=0;i<headers.length;i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=EMS-Store-Movimientos.xlsx").contentType(MediaType.parseMediaType(MIME)).body(out.toByteArray());
        } catch(IOException e){ throw new IllegalStateException("No se pudo generar el reporte de movimientos",e); }
    }
}
