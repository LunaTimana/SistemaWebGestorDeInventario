package com.emsstore.backend.dto;

import java.util.List;

public record ImportacionResponse(
        boolean confirmado,
        int modelosLeidos,
        int productosNuevos,
        int variantesNuevas,
        int variantesOmitidas,
        long unidades,
        List<String> advertencias) {
}