package com.emsstore.backend.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
@Entity
@Table(name="productos")
public class Producto {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @NotBlank @Size(max=100) @Column(name="nombre_modelo",nullable=false,length=100) private String nombreModelo;
 @NotNull @Column(name="marca_id",nullable=false) private Integer marcaId;
 @NotNull @Column(name="categoria_id",nullable=false) private Integer categoriaId;
 @Column(name="proveedor_id") private Integer proveedorId;
 @Size(max=100) private String diseno;
 @Column(columnDefinition="TEXT") private String descripcion;
 @Size(max=255) @Column(name="imagen_url") private String imagenUrl;
 @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) @Column(name="costo_unitario",nullable=false,precision=10,scale=2) private BigDecimal costoUnitario;
 @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) @Column(name="precio_venta",nullable=false,precision=10,scale=2) private BigDecimal precioVenta;
 public Producto() {}
 public Integer getId(){return id;} public void setId(Integer v){id=v;}
 public String getNombreModelo(){return nombreModelo;} public void setNombreModelo(String v){nombreModelo=v;}
 public Integer getMarcaId(){return marcaId;} public void setMarcaId(Integer v){marcaId=v;}
 public Integer getCategoriaId(){return categoriaId;} public void setCategoriaId(Integer v){categoriaId=v;}
 public Integer getProveedorId(){return proveedorId;} public void setProveedorId(Integer v){proveedorId=v;}
 public String getDiseno(){return diseno;} public void setDiseno(String v){diseno=v;}
 public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;}
 public String getImagenUrl(){return imagenUrl;} public void setImagenUrl(String v){imagenUrl=v;}
 public BigDecimal getCostoUnitario(){return costoUnitario;} public void setCostoUnitario(BigDecimal v){costoUnitario=v;}
 public BigDecimal getPrecioVenta(){return precioVenta;} public void setPrecioVenta(BigDecimal v){precioVenta=v;}
}
