package com.emsstore.backend.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginaResponse<T>(
        List<T> contenido,
        long totalElementos,
        int pagina,
        int tamano,
        int totalPaginas) {

    public static <T> PaginaResponse<T> desde(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getTotalElements(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalPages());
    }
}