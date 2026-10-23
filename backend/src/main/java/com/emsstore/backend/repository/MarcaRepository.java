package com.emsstore.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
}