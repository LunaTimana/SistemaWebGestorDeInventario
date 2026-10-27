package com.emsstore.backend.controller;

import com.emsstore.backend.entity.Categoria;
import com.emsstore.backend.entity.Marca;
import com.emsstore.backend.entity.Producto;
import com.emsstore.backend.entity.VarianteProducto;
import com.emsstore.backend.repository.CategoriaRepository;
import com.emsstore.backend.repository.MarcaRepository;
import com.emsstore.backend.repository.ProductoRepository;
import com.emsstore.backend.repository.VarianteProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Exporta las existencias actuales desde MySQL a un archivo Excel .xlsx. */
@RestController
public class InventarioExcelController {
    private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ProductoRepository productoRepository;
    private final VarianteProductoRepository varianteRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    public InventarioExcelController(
            ProductoRepository productoRepository,
            VarianteProductoRepository varianteRepository,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
    }

    @GetMapping(value = "/api/inventario/exportar/excel", produces = XLSX_MIME)
    public ResponseEntity<byte[]> exportarInventario() {
        List<Producto> productos = productoRepository.findAll();
        List<VarianteProducto> variantes = varianteRepository.findAll();
        Map<Integer, Producto> productosPorId = new HashMap<>();
        productos.forEach(p -> productosPorId.put(p.getId(), p));
        Map<Integer, String> categoriasPorId = new HashMap<>();
        for (Categoria categoria : categoriaRepository.findAll()) {
            categoriasPorId.put(categoria.getId(), categoria.getNombre());
        }
        Map<Integer, String> marcasPorId = new HashMap<>();
        for (Marca marca : marcaRepository.findAll()) {
            marcasPorId.put(marca.getId(), marca.getNombre());
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventario");
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, variantes.size()), 0, 9));

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("\"S/ \"#,##0.00"));

            String[] headers = {"SKU", "Producto", "Marca", "Categoría", "Talla", "Color",
                    "Stock actual", "Stock mínimo", "Precio de venta (S/)", "Estado"};
            Row header = sheet.createRow(0);
            header.setHeightInPoints(24);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = header.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (VarianteProducto variante : variantes) {
                Producto producto = productosPorId.get(variante.getProductoId());
                if (producto == null) continue;
                Row row = sheet.createRow(rowIndex++);
                setText(row, 0, variante.getCodigoUnico());
                setText(row, 1, producto.getNombreModelo());
                setText(row, 2, marcasPorId.getOrDefault(producto.getMarcaId(), "Marca #" + producto.getMarcaId()));
                setText(row, 3, categoriasPorId.getOrDefault(producto.getCategoriaId(), "Categoría #" + producto.getCategoriaId()));
                setText(row, 4, variante.getTalla());
                setText(row, 5, variante.getColor());
                setNumber(row, 6, variante.getStockActual() == null ? 0 : variante.getStockActual());
                setNumber(row, 7, variante.getStockMinimo() == null ? 0 : variante.getStockMinimo());
                Cell price = row.createCell(8);
                BigDecimal precio = producto.getPrecioVenta() == null ? BigDecimal.ZERO : producto.getPrecioVenta();
                price.setCellValue(precio.doubleValue());
                price.setCellStyle(moneyStyle);
                int stock = variante.getStockActual() == null ? 0 : variante.getStockActual();
                int minimo = variante.getStockMinimo() == null ? 0 : variante.getStockMinimo();
                setText(row, 9, stock <= 0 ? "Sin Stock" : (stock <= minimo ? "Bajo Stock" : "Disponible"));
            }

            int[] widths = {18, 30, 22, 24, 12, 18, 15, 15, 22, 16};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            workbook.write(output);
            String filename = "EMS-Store-Inventario-" + LocalDate.now() + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(XLSX_MIME))
                    .contentLength(output.size())
                    .body(output.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el archivo Excel del inventario", ex);
        }
    }

    private static void setText(Row row, int column, String value) {
        row.createCell(column, CellType.STRING).setCellValue(value == null ? "" : value);
    }

    private static void setNumber(Row row, int column, int value) {
        row.createCell(column, CellType.NUMERIC).setCellValue(value);
    }
}
