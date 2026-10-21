import { Component } from '@angular/core';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por un servicio con HttpClient.
@Component({
  selector: 'app-reportes',
  imports: [],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss',
})
export class Reportes {
  reportes = [
    { titulo: 'Reporte de Inventario General', descripcion: 'Resumen de existencias y valorización total del catálogo de productos.', icono: 'bi-file-earmark-text', boton: 'Generar PDF', principal: true },
    { titulo: 'Reporte de Movimientos', descripcion: 'Registro completo de las operaciones de entradas y salidas.', icono: 'bi-file-earmark-text', boton: 'Generar Excel', principal: false },
    { titulo: 'Rotación de Inventario', descripcion: 'Indicadores de ventas y velocidad de reposición de artículos.', icono: 'bi-graph-up-arrow', boton: 'Generar PDF', principal: false },
  ];

  categorias = [
    { nombre: 'Calzado Deportivo', valor: 'S/ 180,000.00', porcentaje: 53.5, color: '#b4ff00' },
    { nombre: 'Prendas / Ropa', valor: 'S/ 115,000.00', porcentaje: 34.2, color: '#6b7280' },
    { nombre: 'Accesorios', valor: 'S/ 41,200.00', porcentaje: 12.3, color: '#16a34a' },
  ];

  detalle = [
    { categoria: 'Calzado Deportivo', items: '250 uds', promedio: 'S/ 720.00', valor: 'S/ 180,000.00' },
    { categoria: 'Prendas / Ropa', items: '150 uds', promedio: 'S/ 766.66', valor: 'S/ 115,000.00' },
  ];
}