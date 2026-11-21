package com.emsstore.backend.controller;

import com.emsstore.backend.entity.Producto;
import com.emsstore.backend.entity.VarianteProducto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductoInventarioRequest {
    @NotNull @Valid
    private Producto producto;

    @NotNull @Valid
    private VarianteDatos variante;

    public ProductoInventarioRequest() {}

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public VarianteDatos getVariante() { return variante; }
    public void setVariante(VarianteDatos variante) { this.variante = variante; }

    public static class VarianteDatos {
        @NotBlank @Size(max = 20)
        private String talla;
        @NotBlank @Size(max = 20)
        private String genero;
        @NotBlank @Size(max = 40)
        private String color;
        @NotBlank @Size(max = 50)
        private String codigoUnico;
        @NotNull @Min(0)
        private Integer stockActual;
        @NotNull @Min(0)
        private Integer stockMinimo;

        public String getTalla() { return talla; }
        public void setTalla(String talla) { this.talla = talla; }
        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getCodigoUnico() { return codigoUnico; }
        public void setCodigoUnico(String codigoUnico) { this.codigoUnico = codigoUnico; }
        public Integer getStockActual() { return stockActual; }
        public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
        public Integer getStockMinimo() { return stockMinimo; }
        public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

        public VarianteProducto toEntity() {
            VarianteProducto entity = new VarianteProducto();
            entity.setTalla(talla);
            entity.setGenero(genero);
            entity.setColor(color);
            entity.setCodigoUnico(codigoUnico);
            entity.setStockActual(stockActual);
            entity.setStockMinimo(stockMinimo);
            return entity;
        }
    }
}
