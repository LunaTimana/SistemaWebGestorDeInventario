package com.emsstore.backend.repository;
import com.emsstore.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ProductoRepository extends JpaRepository<Producto,Integer> {
 @Query("select p from Producto p where lower(p.nombreModelo) like lower(concat('%', :q, '%'))")
 List<Producto> buscarPorNombre(String q);
}
