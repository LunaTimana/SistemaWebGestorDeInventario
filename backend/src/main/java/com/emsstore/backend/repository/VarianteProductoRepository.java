package com.emsstore.backend.repository;

import com.emsstore.backend.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Integer> {
    List<VarianteProducto> findByProductoId(Integer productoId);

    @Modifying
    @Query("delete from VarianteProducto v where v.productoId = :productoId")
    int eliminarPorProductoId(@Param("productoId") Integer productoId);

    @Query("select v from VarianteProducto v where v.stockActual < v.stockMinimo")
    List<VarianteProducto> findAlertasStock();
}
