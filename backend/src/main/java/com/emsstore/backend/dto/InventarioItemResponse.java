package com.emsstore.backend.dto;

import java.math.BigDecimal;

import com.emsstore.backend.model.VarianteProducto;

public record InventarioItemResponse(
        Integer id,
        String sku,
        String marca,
        String nombre,
        String categoria,
        String talla,
        String color,
        int stock,
        int stockMinimo,
        BigDecimal precio,
        String estado) {

    public static InventarioItemResponse desde(VarianteProducto v) {
        return new InventarioItemResponse(
                v.getId(),
                v.getCodigoUnico(),
                v.getProducto().getMarca().getNombre(),
                v.getProducto().getNombreModelo(),
                v.getProducto().getCategoria().getNombre(),
                v.getTalla(),
                v.getColor(),
                v.getStockActual(),
                v.getStockMinimo(),
                v.getProducto().getPrecioVenta(),
                calcularEstado(v.getStockActual(), v.getStockMinimo()));
    }

    // Regla de la pantalla de Inventario:
    //   Sin Stock  -> stock = 0
    //   Bajo Stock -> stock por debajo del mínimo
    //   Disponible -> resto
    public static String calcularEstado(int stock, int minimo) {
        if (stock == 0) {
            return "Sin Stock";
        }
        if (stock < minimo) {
            return "Bajo Stock";
        }
        return "Disponible";
    }
}