package com.emsstore.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findFirstByMarcaIdAndNombreModeloIgnoreCase(Integer marcaId, String nombreModelo);
    Optional<Producto> findFirstByMarcaIdAndCategoriaIdAndNombreModeloIgnoreCase(
            Integer marcaId, Integer categoriaId, String nombreModelo);
}