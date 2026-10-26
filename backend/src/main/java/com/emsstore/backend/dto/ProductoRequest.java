package com.emsstore.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull Integer marcaId,
        @NotNull Integer categoriaId,
        Integer proveedorId,
        @NotBlank @Size(max = 20) String talla,
        @NotBlank @Size(max = 40) String color,
        @NotNull @DecimalMin("0.00") BigDecimal costoUnitario,
        @NotNull @DecimalMin("0.00") BigDecimal precioVenta,
        @NotNull @Min(0) Integer cantidadInicial,
        @Min(0) Integer stockMinimo,
        @Size(max = 255) String imagenUrl,
        Integer responsableId) {
}