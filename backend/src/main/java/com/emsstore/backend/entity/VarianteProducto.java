package com.emsstore.backend.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="variantes_producto", uniqueConstraints=@UniqueConstraint(name="uq_variante",columnNames={"producto_id","talla","color"}))
public class VarianteProducto {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
 @NotNull @Column(name="producto_id",nullable=false) private Integer productoId;
 @NotBlank @Size(max=20) @Column(nullable=false,length=20) private String talla;
 @NotBlank @Size(max=40) @Column(nullable=false,length=40) private String color;
 @NotBlank @Size(max=50) @Column(name="codigo_unico",nullable=false,unique=true,length=50) private String codigoUnico;
 @Min(0) @Column(name="stock_actual",nullable=false) private Integer stockActual=0;
 @Min(0) @Column(name="stock_minimo",nullable=false) private Integer stockMinimo=0;
 public VarianteProducto(){}
 public Integer getId(){return id;} public void setId(Integer v){id=v;} public Integer getProductoId(){return productoId;} public void setProductoId(Integer v){productoId=v;}
 public String getTalla(){return talla;} public void setTalla(String v){talla=v;} public String getColor(){return color;} public void setColor(String v){color=v;}
 public String getCodigoUnico(){return codigoUnico;} public void setCodigoUnico(String v){codigoUnico=v;}
 public Integer getStockActual(){return stockActual;} public void setStockActual(Integer v){stockActual=v;}
 public Integer getStockMinimo(){return stockMinimo;} public void setStockMinimo(Integer v){stockMinimo=v;}
}
