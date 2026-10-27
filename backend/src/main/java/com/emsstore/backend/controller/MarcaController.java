package com.emsstore.backend.controller;
import com.emsstore.backend.entity.Marca;
import com.emsstore.backend.repository.MarcaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/marcas")
public class MarcaController {
 private final MarcaRepository repo;
 public MarcaController(MarcaRepository repo){this.repo=repo;}
 @GetMapping public List<Marca> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Marca obtener(@PathVariable Integer id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado"));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Marca crear(@RequestBody Marca item){item.setId(null);return repo.save(item);}
 @PutMapping("/{id}") public Marca actualizar(@PathVariable Integer id,@RequestBody Marca item){Marca actual=obtener(id); item.setId(actual.getId()); return repo.save(item);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Integer id){if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Registro no encontrado"); try{repo.deleteById(id);}catch(Exception e){throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar porque tiene registros asociados");}}
}
