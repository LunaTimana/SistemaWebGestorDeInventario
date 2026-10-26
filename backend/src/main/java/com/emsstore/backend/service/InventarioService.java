package com.emsstore.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.emsstore.backend.dto.HistorialItemResponse;
import com.emsstore.backend.dto.InventarioItemResponse;
import com.emsstore.backend.dto.PaginaResponse;
import com.emsstore.backend.dto.ProductoDetalleResponse;
import com.emsstore.backend.model.VarianteProducto;
import com.emsstore.backend.repository.MovimientoInventarioRepository;
import com.emsstore.backend.repository.VarianteProductoRepository;

@Service
public class InventarioService {

    private static final int TAMANO_MAXIMO = 100;

    private final VarianteProductoRepository variantes;
    private final MovimientoInventarioRepository movimientos;

    public InventarioService(VarianteProductoRepository variantes,
                             MovimientoInventarioRepository movimientos) {
        this.variantes = variantes;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<InventarioItemResponse> buscar(String buscar, String categoria, String talla,
                                                         int pagina, int tamano) {
        int paginaSegura = Math.max(pagina, 0);
        int tamanoSeguro = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO);
        Pageable pageable = PageRequest.of(paginaSegura, tamanoSeguro);

        Page<VarianteProducto> resultado =
                variantes.buscar(limpiar(buscar), limpiar(categoria), limpiar(talla), pageable);

        return PaginaResponse.desde(resultado.map(InventarioItemResponse::desde));
    }

    @Transactional(readOnly = true)
    public ProductoDetalleResponse detalle(String sku) {
        VarianteProducto variante = variantes.buscarPorSku(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        List<HistorialItemResponse> historial = movimientos
                .findTop10ByVarianteIdOrderByFechaDesc(variante.getId())
                .stream()
                .map(HistorialItemResponse::desde)
                .toList();

        return ProductoDetalleResponse.desde(variante, historial);
    }

    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}