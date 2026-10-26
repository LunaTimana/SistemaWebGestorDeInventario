package com.emsstore.backend.dto;

import com.emsstore.backend.model.VarianteProducto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoDetalleResponse(
        Integer id,
        String sku,
        String marca,
        String nombre,
        String categoria,
        String talla,
        String color,
        BigDecimal precio,
        int stock,
        int stockMinimo,
        String estado,
        List<HistorialItemResponse> historial) {

    public static ProductoDetalleResponse desde(VarianteProducto v, List<HistorialItemResponse> historial) {
        return new ProductoDetalleResponse(
                v.getId(),
                v.getCodigoUnico(),
                v.getProducto().getMarca().getNombre(),
                v.getProducto().getNombreModelo(),
                v.getProducto().getCategoria().getNombre(),
                v.getTalla(),
                v.getColor(),
                v.getProducto().getPrecioVenta(),
                v.getStockActual(),
                v.getStockMinimo(),
                InventarioItemResponse.calcularEstado(v.getStockActual(), v.getStockMinimo()),
                historial);
    }
}