package com.emsstore.backend.entity;
import jakarta.persistence.*;
@Entity @Table(name="proveedores")
public class Proveedor { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id; @Column(nullable=false,length=120) private String nombre; @Column(length=120) private String contacto; @Column(length=30) private String telefono; @Column(length=150) private String correo;
 public Proveedor(){} public Integer getId(){return id;} public void setId(Integer id){this.id=id;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;} public String getContacto(){return contacto;} public void setContacto(String v){contacto=v;} public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;} public String getCorreo(){return correo;} public void setCorreo(String v){correo=v;} }
