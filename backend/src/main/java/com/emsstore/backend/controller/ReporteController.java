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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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

    @GetMapping(value="/inventario/pdf", produces="application/pdf")
    public ResponseEntity<byte[]> exportarInventarioPdf() {
        List<Map<String,Object>> rows = jdbc.queryForList("""
            SELECT v.codigo_unico AS sku,p.nombre_modelo AS producto,ma.nombre AS marca,c.nombre AS categoria,
                   v.talla,v.color,v.stock_actual AS stock,v.stock_minimo AS minimo,p.precio_venta AS precio
            FROM variantes_producto v JOIN productos p ON p.id=v.producto_id
            LEFT JOIN marcas ma ON ma.id=p.marca_id LEFT JOIN categorias c ON c.id=p.categoria_id
            ORDER BY p.nombre_modelo,v.talla,v.color
            """);
        return pdf("Reporte de Inventario General", new String[]{"SKU","Producto","Marca","Categoría","Talla","Color","Stock","Mínimo","Precio"}, rows,
            new String[]{"sku","producto","marca","categoria","talla","color","stock","minimo","precio"}, "EMS-Store-Inventario.pdf", true);
    }

    @GetMapping(value="/movimientos/pdf", produces="application/pdf")
    public ResponseEntity<byte[]> exportarMovimientosPdf() {
        List<Map<String,Object>> rows = jdbc.queryForList("""
            SELECT m.fecha,m.tipo,m.motivo,v.codigo_unico AS sku,p.nombre_modelo AS producto,
                   m.cantidad,CONCAT(u.nombre,' ',u.apellido) AS responsable,m.documento
            FROM movimientos_inventario m JOIN variantes_producto v ON v.id=m.variante_id
            JOIN productos p ON p.id=v.producto_id JOIN usuarios u ON u.id=m.usuario_id
            ORDER BY m.fecha DESC,m.id DESC
            """);
        return pdf("Reporte de Movimientos", new String[]{"Fecha","Tipo","Motivo","SKU","Producto","Cantidad","Responsable","Documento"}, rows,
            new String[]{"fecha","tipo","motivo","sku","producto","cantidad","responsable","documento"}, "EMS-Store-Movimientos.pdf", true);
    }

    private ResponseEntity<byte[]> pdf(String title, String[] headers, List<Map<String,Object>> rows,
                                       String[] keys, String filename, boolean landscape) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(landscape ? PageSize.A4.rotate() : PageSize.A4, 24, 24, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(title));
            document.add(new Paragraph("Generado: " + java.time.LocalDate.now()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            for (String header : headers) table.addCell(new Phrase(header));
            for (Map<String,Object> row : rows) {
                for (String key : keys) {
                    Object value = row.get(key);
                    table.addCell(value == null ? "" : value.toString());
                }
            }
            document.add(table);
            document.close();
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF).contentLength(out.size()).body(out.toByteArray());
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("No se pudo generar " + filename, e);
        }
    }
}
