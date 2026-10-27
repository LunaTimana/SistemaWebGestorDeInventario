package com.emsstore.backend.controller;
import com.emsstore.backend.entity.Categoria;
import com.emsstore.backend.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/categorias")
public class CategoriaController {
 private final CategoriaRepository repo;
 public CategoriaController(CategoriaRepository repo){this.repo=repo;}
 @GetMapping public List<Categoria> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Categoria obtener(@PathVariable Integer id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado"));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Categoria crear(@RequestBody Categoria item){item.setId(null);return repo.save(item);}
 @PutMapping("/{id}") public Categoria actualizar(@PathVariable Integer id,@RequestBody Categoria item){Categoria actual=obtener(id); item.setId(actual.getId()); return repo.save(item);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Integer id){if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado"); try{repo.deleteById(id);}catch(Exception e){throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar porque tiene registros asociados");}}
}
