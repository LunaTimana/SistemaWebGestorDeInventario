package com.emsstore.backend.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lee el Excel de stock de la tienda: una fila por modelo y una columna por talla.
 * Solo toma el STOCK ACTUAL; el historial semanal de ventas no se importa.
 */
@Component
public class ExcelStockReader {

    private static final List<String> TALLAS_ADULTOS = List.of("S", "M", "L", "XL", "XXL");
    private static final List<String> TALLAS_NINOS = List.of("6", "8", "10", "12", "14", "16");

    // Los índices cuentan desde 0: la columna C es 2, la J es 9 y la K es 10
    private static final int COLUMNA_MODELO = 2;
    private static final int PRIMERA_FILA = 6; // fila 7 de Excel

    private record ConfigHoja(String hoja, String categoria, int columnaStockActual, List<String> tallas) {
    }

    private static final List<ConfigHoja> HOJAS = List.of(
            new ConfigHoja("COLECCION ALIANZA", "Colección Alianza", 9, TALLAS_ADULTOS),
            new ConfigHoja("COLECCION UNIVERSITARIO2", "Colección Universitario", 9, TALLAS_ADULTOS),
            new ConfigHoja("INTERNACIONALES2", "Internacionales", 9, TALLAS_ADULTOS),
            new ConfigHoja("INTERNACIONALES RETRO", "Internacionales Retro", 9, TALLAS_ADULTOS),
            new ConfigHoja("NIÑOS", "Niños", 10, TALLAS_NINOS));

    public record ModeloLeido(String hoja, int filaExcel, String categoria, String nombre,
                              Map<String, Integer> stockPorTalla) {
    }

    public record Lectura(List<ModeloLeido> modelos, List<String> advertencias) {
    }

    public Lectura leer(InputStream entrada) throws IOException {
        List<ModeloLeido> modelos = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        try (Workbook libro = new XSSFWorkbook(entrada)) {
            for (ConfigHoja config : HOJAS) {
                Sheet hoja = libro.getSheet(config.hoja());
                if (hoja == null) {
                    advertencias.add("No se encontró la hoja \"" + config.hoja() + "\"; se omitió.");
                    continue;
                }
                leerHoja(hoja, config, modelos, advertencias);
            }
        }
        return new Lectura(modelos, advertencias);
    }

    private void leerHoja(Sheet hoja, ConfigHoja config, List<ModeloLeido> modelos, List<String> advertencias) {
        Map<String, ModeloLeido> vistos = new HashMap<>();
        int sinStock = 0;

        for (int i = PRIMERA_FILA; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            if (fila == null) {
                continue;
            }
            int filaExcel = i + 1;

            Cell celdaModelo = fila.getCell(COLUMNA_MODELO);
            if (celdaModelo == null || celdaModelo.getCellType() == CellType.BLANK) {
                continue; // fila vacía
            }
            if (celdaModelo.getCellType() != CellType.STRING) {
                advertencias.add(String.format(
                        "Hoja \"%s\", fila %d: el nombre del modelo no es texto; se omitió.",
                        config.hoja(), filaExcel));
                continue;
            }
            String nombre = limpiarNombre(celdaModelo.getStringCellValue());
            if (nombre.isEmpty()) {
                continue;
            }

            // Stock actual por talla: solo se guardan las tallas con stock mayor que cero
            Map<String, Integer> stock = new LinkedHashMap<>();
            for (int t = 0; t < config.tallas().size(); t++) {
                String talla = config.tallas().get(t);
                Integer valor = numeroEntero(fila.getCell(config.columnaStockActual() + t));
                if (valor != null && valor < 0) {
                    advertencias.add(String.format(
                            "Hoja \"%s\", fila %d (%s), talla %s: stock negativo (%d); se importa como 0.",
                            config.hoja(), filaExcel, nombre, talla, valor));
                    valor = 0;
                }
                if (valor != null && valor > 0) {
                    stock.put(talla, valor);
                }
            }
            if (stock.isEmpty()) {
                sinStock++;
                continue;
            }

            String clave = nombre.toLowerCase(Locale.ROOT);
            ModeloLeido previo = vistos.get(clave);
            if (previo != null) {
                // Mismo nombre en otra fila de la misma hoja: se suma el stock para no perderlo
                stock.forEach((talla, cantidad) -> previo.stockPorTalla().merge(talla, cantidad, Integer::sum));
                advertencias.add(String.format(
                        "Hoja \"%s\", fila %d: el modelo \"%s\" está repetido; se sumó su stock al de la fila %d.",
                        config.hoja(), filaExcel, nombre, previo.filaExcel()));
                continue;
            }

            ModeloLeido modelo = new ModeloLeido(config.hoja(), filaExcel, config.categoria(), nombre, stock);
            vistos.put(clave, modelo);
            modelos.add(modelo);
        }

        if (sinStock > 0) {
            advertencias.add(String.format(
                    "Hoja \"%s\": %d modelos sin stock; no se importan.", config.hoja(), sinStock));
        }
    }

    // Lee un número, también cuando la celda es una fórmula (usa el valor ya calculado por Excel)
    private Integer numeroEntero(Cell celda) {
        if (celda == null) {
            return null;
        }
        CellType tipo = celda.getCellType();
        if (tipo == CellType.FORMULA) {
            tipo = celda.getCachedFormulaResultType();
        }
        if (tipo == CellType.NUMERIC) {
            return (int) Math.round(celda.getNumericCellValue());
        }
        return null;
    }

    // Quita espacios repetidos y corrige el error de escritura "Adiddas"
    private String limpiarNombre(String texto) {
        return texto.replaceAll("[\\s\\u00A0]+", " ").trim().replaceAll("(?i)adiddas", "Adidas");
    }
}