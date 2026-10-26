package com.emsstore.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Marca;

import java.util.Optional;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
        Optional<Marca> findByNombreIgnoreCase(String nombre);
}