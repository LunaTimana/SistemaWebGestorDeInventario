import { Component } from '@angular/core';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por un servicio con HttpClient.
@Component({
  selector: 'app-alertas',
  imports: [],
  templateUrl: './alertas.html',
  styleUrl: './alertas.scss',
})
export class Alertas {
  resumen = [
    { titulo: 'Productos Críticos', valor: '4 ítems', tono: 'rojo' },
    { titulo: 'Productos en Riesgo', valor: '10 ítems', tono: 'naranja' },
    { titulo: 'Total Monitoreados', valor: '184 ítems', tono: 'neutro' },
  ];

  alertas = [
    { sku: 'UA-STORM-L', producto: 'Cortavientos Storm Impermeable', stock: '0 uds', minimo: '10 uds', deficit: '-10 uds', estado: 'CRÍTICO', clase: 'rojo', ultimoIngreso: '15/10/2026' },
    { sku: 'AD-TFIT-M', producto: 'Polera de Entrenamiento TechFit', stock: '8 uds', minimo: '15 uds', deficit: '-7 uds', estado: 'RIESGO', clase: 'naranja', ultimoIngreso: '18/10/2026' },
    { sku: 'NK-PEG40-10', producto: 'Zapatillas Pegasus 40 Zoom', stock: '12 uds', minimo: '15 uds', deficit: '-3 uds', estado: 'RIESGO', clase: 'naranja', ultimoIngreso: '20/10/2026' },
    { sku: 'PM-COOL-S', producto: 'Camiseta Puma Cool Running', stock: '1 ud', minimo: '8 uds', deficit: '-9 uds', estado: 'CRÍTICO', clase: 'rojo', ultimoIngreso: '05/10/2026' },
  ];
}