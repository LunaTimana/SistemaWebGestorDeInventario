package com.emsstore.backend.model;

// Los valores van en minúscula a propósito: deben coincidir con el
// ENUM('activo', 'inactivo') de MySQL, que distingue cómo se leen.
public enum EstadoUsuario {
    activo,
    inactivo
}