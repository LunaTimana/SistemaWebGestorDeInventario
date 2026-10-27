package com.emsstore.backend.controller;

import com.emsstore.backend.entity.Producto;
import com.emsstore.backend.repository.ProductoRepository;
import com.emsstore.backend.repository.VarianteProductoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoRepository repo;
    private final VarianteProductoRepository varianteRepo;
    private final JdbcTemplate jdbc;

    public ProductoController(ProductoRepository repo, VarianteProductoRepository varianteRepo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.varianteRepo = varianteRepo;
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String q) {
        return (q == null || q.isBlank()) ? repo.findAll() : repo.buscarPorNombre(q.trim());
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Integer id) {
        return repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado")
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@Valid @RequestBody Producto producto) {
        producto.setId(null);
        return repo.save(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Integer id, @Valid @RequestBody Producto datos) {
        Producto p = obtener(id);
        p.setNombreModelo(datos.getNombreModelo());
        p.setMarcaId(datos.getMarcaId());
        p.setCategoriaId(datos.getCategoriaId());
        p.setProveedorId(datos.getProveedorId());
        p.setDiseno(datos.getDiseno());
        p.setDescripcion(datos.getDescripcion());
        p.setImagenUrl(datos.getImagenUrl());
        p.setCostoUnitario(datos.getCostoUnitario());
        p.setPrecioVenta(datos.getPrecioVenta());
        return repo.save(p);
    }

    @PostMapping("/completo")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public java.util.Map<String, Object> crearCompleto(@Valid @RequestBody ProductoInventarioRequest request, Authentication auth) {
        Producto producto = request.getProducto();
        producto.setId(null);
        Producto guardado = repo.saveAndFlush(producto);

        var variante = request.getVariante();
        variante.setId(null);
        variante.setProductoId(guardado.getId());
        var varianteGuardada = varianteRepo.saveAndFlush(variante);
        if (varianteGuardada.getStockActual() != null && varianteGuardada.getStockActual() > 0) {
            Integer userId = (Integer) auth.getPrincipal();
            jdbc.update("INSERT INTO movimientos_inventario (variante_id,tipo,motivo,cantidad,usuario_id,documento,comentario) VALUES (?,?,?,?,?,?,?)",
                    varianteGuardada.getId(), "entrada", "ajuste", varianteGuardada.getStockActual(), userId, "Stock inicial", "Registro de stock inicial al crear producto");
        }

        return java.util.Map.of("producto", guardado, "variante", varianteGuardada);
    }

    @PutMapping("/{id}/completo/{varianteId}")
    @Transactional
    public java.util.Map<String, Object> actualizarCompleto(
            @PathVariable Integer id,
            @PathVariable Integer varianteId,
            @Valid @RequestBody ProductoInventarioRequest request, Authentication auth) {
        Producto actual = obtener(id);
        Producto datos = request.getProducto();
        actual.setNombreModelo(datos.getNombreModelo());
        actual.setMarcaId(datos.getMarcaId());
        actual.setCategoriaId(datos.getCategoriaId());
        actual.setProveedorId(datos.getProveedorId());
        actual.setDiseno(datos.getDiseno());
        actual.setDescripcion(datos.getDescripcion());
        actual.setImagenUrl(datos.getImagenUrl());
        actual.setCostoUnitario(datos.getCostoUnitario());
        actual.setPrecioVenta(datos.getPrecioVenta());
        Producto guardado = repo.saveAndFlush(actual);

        var variante = varianteRepo.findById(varianteId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada")
        );
        if (!variante.getProductoId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La variante no pertenece al producto indicado");
        }
        var datosVariante = request.getVariante();
        int stockAnterior = variante.getStockActual() == null ? 0 : variante.getStockActual();
        int stockNuevo = datosVariante.getStockActual() == null ? 0 : datosVariante.getStockActual();
        variante.setTalla(datosVariante.getTalla());
        variante.setColor(datosVariante.getColor());
        variante.setCodigoUnico(datosVariante.getCodigoUnico());
        variante.setStockActual(datosVariante.getStockActual());
        variante.setStockMinimo(datosVariante.getStockMinimo());
        var varianteGuardada = varianteRepo.saveAndFlush(variante);
        if (stockAnterior != stockNuevo) {
            Integer userId = (Integer) auth.getPrincipal();
            String tipo = stockNuevo > stockAnterior ? "entrada" : "salida";
            jdbc.update("INSERT INTO movimientos_inventario (variante_id,tipo,motivo,cantidad,usuario_id,documento,comentario) VALUES (?,?,?,?,?,?,?)",
                    varianteGuardada.getId(), tipo, "ajuste", Math.abs(stockNuevo - stockAnterior), userId, "Ajuste desde edición de producto",
                    "Stock modificado desde el formulario de edición: " + stockAnterior + " -> " + stockNuevo);
        }

        return java.util.Map.of("producto", guardado, "variante", varianteGuardada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void eliminar(@PathVariable Integer id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

        try {
            // Se eliminan primero las variantes; la transacción revierte todo
            // si alguna variante tiene movimientos históricos asociados.
            varianteRepo.eliminarPorProductoId(id);
            repo.deleteById(id);
            repo.flush();
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar el producto porque tiene movimientos o registros asociados."
            );
        }
    }
}
