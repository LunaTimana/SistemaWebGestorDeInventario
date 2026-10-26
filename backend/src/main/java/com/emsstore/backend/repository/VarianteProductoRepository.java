package com.emsstore.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.emsstore.backend.model.VarianteProducto;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Integer> {

    boolean existsByProductoIdAndTallaAndColor(Integer productoId, String talla, String color);

    boolean existsByCodigoUnico(String codigoUnico);

    @Query(value = """
            select v from VarianteProducto v
            join fetch v.producto p
            join fetch p.marca
            join fetch p.categoria c
            where (:buscar is null
                   or lower(p.nombreModelo) like lower(concat('%', :buscar, '%'))
                   or lower(v.codigoUnico) like lower(concat('%', :buscar, '%')))
              and (:categoria is null or c.nombre = :categoria)
              and (:talla is null or v.talla = :talla)
            order by p.nombreModelo, v.talla
            """,
            countQuery = """
            select count(v) from VarianteProducto v
            join v.producto p
            join p.categoria c
            where (:buscar is null
                   or lower(p.nombreModelo) like lower(concat('%', :buscar, '%'))
                   or lower(v.codigoUnico) like lower(concat('%', :buscar, '%')))
              and (:categoria is null or c.nombre = :categoria)
              and (:talla is null or v.talla = :talla)
            """)
    Page<VarianteProducto> buscar(@Param("buscar") String buscar,
                                  @Param("categoria") String categoria,
                                  @Param("talla") String talla,
                                  Pageable pageable);
    
        @Query("""
            select v from VarianteProducto v
            join fetch v.producto p
            join fetch p.marca
            join fetch p.categoria
            where v.codigoUnico = :sku
            """)
    Optional<VarianteProducto> buscarPorSku(@Param("sku") String sku);
}