import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por un servicio con HttpClient.
@Component({
  selector: 'app-inventario',
  imports: [RouterLink],
  templateUrl: './inventario.html',
  styleUrl: './inventario.scss',
})
export class Inventario {
  productos = [
    { sku: 'NK-PEG40-10', marca: 'Nike', nombre: 'Zapatillas Pegasus 40 Zoom', categoria: 'Calzado', talla: '42', color: 'Azul/Rojo', stock: 15, precio: 120, estado: 'Disponible', claseEstado: 'disponible' },
    { sku: 'AD-TFIT-M', marca: 'Adidas', nombre: 'Polera de Entrenamiento TechFit', categoria: 'Ropa', talla: 'M', color: 'Negro', stock: 8, precio: 29.99, estado: 'Bajo Stock', claseEstado: 'bajo' },
    { sku: 'UA-STORM-L', marca: 'Under Armour', nombre: 'Cortavientos Storm Impermeable', categoria: 'Ropa', talla: 'L', color: 'Gris', stock: 0, precio: 85, estado: 'Sin Stock', claseEstado: 'sin' },
    { sku: 'NK-DRIT-S', marca: 'Nike', nombre: 'Shorts de Running Dri-FIT Flex', categoria: 'Ropa', talla: 'S', color: 'Azul Marino', stock: 24, precio: 39.99, estado: 'Disponible', claseEstado: 'disponible' },
    { sku: 'PM-DEVI-43', marca: 'Puma', nombre: 'Zapatillas Deviate Nitro Elite', categoria: 'Calzado', talla: '43', color: 'Neon Verde', stock: 12, precio: 180, estado: 'Disponible', claseEstado: 'disponible' },
  ];
}