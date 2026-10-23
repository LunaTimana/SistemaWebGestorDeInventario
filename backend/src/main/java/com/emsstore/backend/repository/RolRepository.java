package com.emsstore.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
}