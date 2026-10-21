import { Component } from '@angular/core';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por un servicio con HttpClient.
@Component({
  selector: 'app-usuarios',
  imports: [],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss',
})
export class Usuarios {
  usuarios = [
    { nombre: 'Marcio Morales', correo: 'mmorales@emsstore.com', rol: 'Administrador', claseRol: 'admin', estado: 'Activo', claseEstado: 'activo', conexion: 'Hoy, 15:45' },
    { nombre: 'Carlos Mendoza', correo: 'cmendoza@emsstore.com', rol: 'Almacén', claseRol: 'almacen', estado: 'Activo', claseEstado: 'activo', conexion: 'Hoy, 08:30' },
    { nombre: 'Luz Benitez', correo: 'lbenitez@emsstore.com', rol: 'Ventas', claseRol: 'ventas', estado: 'Activo', claseEstado: 'activo', conexion: 'Ayer, 18:22' },
    { nombre: 'Juan Sanchez', correo: 'jsanchez@emsstore.com', rol: 'Almacén', claseRol: 'almacen', estado: 'Inactivo', claseEstado: 'inactivo', conexion: 'Hace 5 días' },
  ];
}