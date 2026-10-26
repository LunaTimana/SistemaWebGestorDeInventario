package com.emsstore.backend.repository;

import com.emsstore.backend.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    // Spring genera la consulta a partir del nombre del método:
    // los 10 movimientos más recientes de una variante
    List<MovimientoInventario> findTop10ByVarianteIdOrderByFechaDesc(Integer varianteId);
}