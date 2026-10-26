package com.emsstore.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emsstore.backend.dto.InventarioItemResponse;
import com.emsstore.backend.dto.PaginaResponse;
import com.emsstore.backend.dto.ProductoDetalleResponse;
import com.emsstore.backend.service.InventarioService;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventario;

    public InventarioController(InventarioService inventario) {
        this.inventario = inventario;
    }

    @GetMapping
    public PaginaResponse<InventarioItemResponse> listar(
            @RequestParam(name = "buscar", required = false) String buscar,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "talla", required = false) String talla,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamano", defaultValue = "10") int tamano) {
        return inventario.buscar(buscar, categoria, talla, pagina, tamano);
    }

    @GetMapping("/{sku}")
    public ProductoDetalleResponse detalle(@PathVariable("sku") String sku) {
        return inventario.detalle(sku);
    }
}