package com.emsstore.backend.entity;
import jakarta.persistence.*;
@Entity @Table(name="marcas")
public class Marca { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id; @Column(nullable=false,unique=true,length=60) private String nombre;
 public Marca(){} public Integer getId(){return id;} public void setId(Integer id){this.id=id;} public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;} }
