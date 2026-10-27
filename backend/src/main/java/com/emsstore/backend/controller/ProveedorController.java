package com.emsstore.backend.controller;
import com.emsstore.backend.entity.Proveedor;
import com.emsstore.backend.repository.ProveedorRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/proveedores")
public class ProveedorController {
 private final ProveedorRepository repo;
 public ProveedorController(ProveedorRepository repo){this.repo=repo;}
 @GetMapping public List<Proveedor> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Proveedor obtener(@PathVariable Integer id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado"));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Proveedor crear(@RequestBody Proveedor item){item.setId(null);return repo.save(item);}
 @PutMapping("/{id}") public Proveedor actualizar(@PathVariable Integer id,@RequestBody Proveedor item){Proveedor actual=obtener(id); item.setId(actual.getId()); return repo.save(item);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Integer id){if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado"); try{repo.deleteById(id);}catch(Exception e){throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar porque tiene registros asociados");}}
}
