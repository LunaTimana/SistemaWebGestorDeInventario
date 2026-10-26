package com.emsstore.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emsstore.backend.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}