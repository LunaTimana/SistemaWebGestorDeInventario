package com.emsstore.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.emsstore.backend.dto.ImportacionResponse;
import com.emsstore.backend.service.ImportacionStockService;

@RestController
@RequestMapping("/api/importaciones")
public class ImportacionController {

    private final ImportacionStockService importacion;

    public ImportacionController(ImportacionStockService importacion) {
        this.importacion = importacion;
    }

    @PostMapping(value = "/stock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacionResponse importarStock(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(name = "responsableId", required = false) Integer responsableId,
            @RequestParam(name = "confirmar", defaultValue = "false") boolean confirmar) {
        return importacion.procesar(archivo, responsableId, confirmar);
    }
}