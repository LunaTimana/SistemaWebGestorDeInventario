import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se buscan por el SKU de la ruta.
@Component({
  selector: 'app-detalle-producto',
  imports: [RouterLink],
  templateUrl: './detalle-producto.html',
  styleUrl: './detalle-producto.scss',
})
export class DetalleProducto {
  producto = {
    marca: 'Nike',
    nombre: 'Zapatillas Pegasus 40 Zoom',
    sku: 'NK-PEG40-10',
    categoria: 'Calzado Deportivo',
    talla: 'US 10',
    color: 'Azul / Rojo',
    precio: 'S/ 120.00',
    stock: '15 unidades',
    estado: 'STOCK ADECUADO',
  };

  historial = [
    { titulo: 'Ingreso Stock', fecha: '28/10/2026', cantidad: '+15 uds', clase: 'verde' },
    { titulo: 'Salida Venta', fecha: '25/10/2026', cantidad: '-1 ud', clase: 'naranja' },
    { titulo: 'Salida Venta', fecha: '24/10/2026', cantidad: '-2 uds', clase: 'naranja' },
    { titulo: 'Ingreso Inicial', fecha: '20/10/2026', cantidad: '+3 uds', clase: 'verde' },
  ];
}