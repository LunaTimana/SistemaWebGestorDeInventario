package com.emsstore.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
        Optional<Categoria> findByNombreIgnoreCase(String nombre);
}