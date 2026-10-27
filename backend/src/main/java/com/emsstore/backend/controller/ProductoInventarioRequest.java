package com.emsstore.backend.controller;

import com.emsstore.backend.entity.Producto;
import com.emsstore.backend.entity.VarianteProducto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ProductoInventarioRequest {
    @NotNull @Valid
    private Producto producto;

    @NotNull @Valid
    private VarianteProducto variante;

    public ProductoInventarioRequest() {}

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public VarianteProducto getVariante() { return variante; }
    public void setVariante(VarianteProducto variante) { this.variante = variante; }
}
