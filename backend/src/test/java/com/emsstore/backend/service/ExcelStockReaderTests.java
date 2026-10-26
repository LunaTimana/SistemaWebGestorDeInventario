package com.emsstore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelStockReaderTests {

    @Test
    void leeModeloYStockDeUnXlsx() throws Exception {
        byte[] archivo;
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet("COLECCION ALIANZA");
            Row fila = hoja.createRow(6);
            fila.createCell(2).setCellValue("Adiddas Local");
            fila.createCell(9).setCellValue(3);
            fila.createCell(10).setCellValue(2);
            libro.write(salida);
            archivo = salida.toByteArray();
        }

        var lectura = new ExcelStockReader().leer(new ByteArrayInputStream(archivo));

        assertEquals(1, lectura.modelos().size());
        assertEquals("Adidas Local", lectura.modelos().getFirst().nombre());
        assertEquals(3, lectura.modelos().getFirst().stockPorTalla().get("S"));
        assertEquals(2, lectura.modelos().getFirst().stockPorTalla().get("M"));
        assertTrue(lectura.advertencias().stream().anyMatch(m -> m.contains("No se encontró la hoja")));
    }
}
