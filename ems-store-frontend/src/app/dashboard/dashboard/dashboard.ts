import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por un servicio con HttpClient.
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  kpis = [
    { titulo: 'Total de Productos en Stock', valor: '450 prendas', etiqueta: '+12%', detalle: 'este mes', icono: 'bi-bag-check', tono: 'verde', tonoEtiqueta: 'verde' },
    { titulo: 'Valor Comercial', valor: 'S/ 342,800', etiqueta: '+8.4%', detalle: 'este mes', icono: 'bi-cash-stack', tono: 'verde', tonoEtiqueta: 'verde' },
    { titulo: 'Alertas de Stock Bajo', valor: '14 ítems', etiqueta: 'Crítico', detalle: 'Requiere Acción', icono: 'bi-exclamation-triangle', tono: 'rojo', tonoEtiqueta: 'rojo' },
    { titulo: 'Movimientos Registrados (Hoy)', valor: '189 movimientos', etiqueta: '+24 nuevo', detalle: 'desde ayer', icono: 'bi-arrow-left-right', tono: 'naranja', tonoEtiqueta: 'verde' },
  ];

    categorias = [
    { nombre: 'Calzado Deportivo', unidades: 250, porcentaje: 55, color: '#b4ff00' },
    { nombre: 'Prendas / Ropa', unidades: 150, porcentaje: 33, color: '#6b7280' },
    { nombre: 'Accesorios', unidades: 50, porcentaje: 12, color: '#16a34a' },
  ];

  alertas = [
    { producto: 'Zapatillas Pegasus 40 - Running', sku: 'NK-PEG40-10', talla: 'US 10', stock: '2 uds', minimo: 15 },
    { producto: 'Polera de Entrenamiento TechFit', sku: 'AD-TFIT-M', talla: 'M', stock: '1 ud', minimo: 10 },
  ];

  transacciones = [
    { titulo: 'Ingreso de Stock Nike', detalle: '+45 unidades ingresadas', color: '#16c26b' },
    { titulo: 'Salida de Stock Adidas', detalle: '-2 unidades (Boleta #4321)', color: '#f59e0b' },
  ];
}