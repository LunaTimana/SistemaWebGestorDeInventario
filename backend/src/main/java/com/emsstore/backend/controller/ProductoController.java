package com.emsstore.backend.controller;

import com.emsstore.backend.dto.ProductoDetalleResponse;
import com.emsstore.backend.dto.ProductoRequest;
import com.emsstore.backend.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDetalleResponse registrar(@Valid @RequestBody ProductoRequest solicitud) {
        return productos.registrar(solicitud);
    }
}