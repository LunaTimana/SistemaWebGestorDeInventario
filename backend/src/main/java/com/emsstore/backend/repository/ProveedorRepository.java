package com.emsstore.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
}