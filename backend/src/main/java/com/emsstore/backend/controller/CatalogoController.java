package com.emsstore.backend.controller;

import com.emsstore.backend.model.Categoria;
import com.emsstore.backend.model.Marca;
import com.emsstore.backend.model.Proveedor;
import com.emsstore.backend.model.Rol;
import com.emsstore.backend.repository.CategoriaRepository;
import com.emsstore.backend.repository.MarcaRepository;
import com.emsstore.backend.repository.ProveedorRepository;
import com.emsstore.backend.repository.RolRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final RolRepository roles;
    private final MarcaRepository marcas;
    private final CategoriaRepository categorias;
    private final ProveedorRepository proveedores;

    public CatalogoController(RolRepository roles, MarcaRepository marcas,
                              CategoriaRepository categorias, ProveedorRepository proveedores) {
        this.roles = roles;
        this.marcas = marcas;
        this.categorias = categorias;
        this.proveedores = proveedores;
    }

    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return roles.findAll();
    }

    @GetMapping("/marcas")
    public List<Marca> listarMarcas() {
        return marcas.findAll();
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return categorias.findAll();
    }

    @GetMapping("/proveedores")
    public List<Proveedor> listarProveedores() {
        return proveedores.findAll();
    }
}