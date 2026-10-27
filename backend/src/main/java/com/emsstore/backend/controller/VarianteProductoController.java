package com.emsstore.backend.controller;

import com.emsstore.backend.entity.VarianteProducto;
import com.emsstore.backend.repository.VarianteProductoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/variantes")
public class VarianteProductoController {
    private final VarianteProductoRepository repo;
    public VarianteProductoController(VarianteProductoRepository repo) { this.repo = repo; }

    @GetMapping public List<VarianteProducto> listar(@RequestParam(required=false) Integer productoId) { return productoId == null ? repo.findAll() : repo.findByProductoId(productoId); }
    @GetMapping("/alertas-stock") public List<VarianteProducto> alertas() { return repo.findAlertasStock(); }
    @GetMapping("/{id}") public VarianteProducto obtener(@PathVariable Integer id) { return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Variante no encontrada")); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public VarianteProducto crear(@Valid @RequestBody VarianteProducto datos) {
        datos.setId(null);
        // El stock inicial debe entrar por movimientos para que siempre exista trazabilidad.
        if (datos.getStockActual() != null && datos.getStockActual() > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Registra el stock inicial mediante el endpoint de movimientos.");
        datos.setStockActual(0);
        if (datos.getStockMinimo() == null) datos.setStockMinimo(0);
        return repo.save(datos);
    }

    @PutMapping("/{id}")
    public VarianteProducto actualizar(@PathVariable Integer id, @Valid @RequestBody VarianteProducto datos) {
        VarianteProducto v = obtener(id);
        if (datos.getStockActual() != null && !datos.getStockActual().equals(v.getStockActual()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No modifiques el stock directamente; registra una entrada, salida o ajuste.");
        v.setProductoId(datos.getProductoId()); v.setTalla(datos.getTalla()); v.setColor(datos.getColor());
        v.setCodigoUnico(datos.getCodigoUnico()); v.setStockMinimo(datos.getStockMinimo());
        return repo.save(v);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Variante no encontrada");
        try { repo.deleteById(id); } catch (Exception e) { throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar una variante con movimientos asociados."); }
    }
}
