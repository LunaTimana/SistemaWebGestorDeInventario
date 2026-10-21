import { Component } from '@angular/core';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por un servicio con HttpClient.
@Component({
  selector: 'app-movimientos',
  imports: [],
  templateUrl: './movimientos.html',
  styleUrl: './movimientos.scss',
})
export class Movimientos {
  resumen = [
    { titulo: 'Total entradas', subtitulo: 'Ingresos registrados', insignia: '+45 uds', valor: '+45 uds', detalle: '2 movimientos de ingreso registrados en el período seleccionado.', icono: 'bi-plus-circle', tono: 'verde' },
    { titulo: 'Total salidas', subtitulo: 'Salidas registradas', insignia: '-3 uds', valor: '-3 uds', detalle: '2 movimientos de salida registrados en el período seleccionado.', icono: 'bi-dash-circle', tono: 'rojo' },
    { titulo: 'Ajustes y pérdidas', subtitulo: 'Movimientos no comerciales', insignia: '-1 ud', valor: '-1 ud', detalle: '1 ajuste o pérdida registrada en el período seleccionado.', icono: 'bi-search', tono: 'naranja' },
  ];

  movimientos = [
    { id: 'MV-9284', fecha: '26/10/2026 14:32', tipo: 'Salida', clase: 'rojo', sku: 'NK-DRIT-S', producto: 'Shorts de Running Dri-FIT Flex', cantidad: '-2 uds', responsable: 'Marcio Morales', origen: 'Boleta #4325' },
    { id: 'MV-9283', fecha: '26/10/2026 11:15', tipo: 'Ingreso', clase: 'verde', sku: 'NK-PEG40-10', producto: 'Zapatillas Pegasus 40 Zoom', cantidad: '+45 uds', responsable: 'Carlos Mendoza', origen: 'Guía #9021' },
    { id: 'MV-9282', fecha: '25/10/2026 17:45', tipo: 'Ajuste', clase: 'naranja', sku: 'UA-STORM-L', producto: 'Cortavientos Storm Impermeable', cantidad: '-1 ud', responsable: 'Marcio Morales', origen: 'Físico Audit' },
    { id: 'MV-9281', fecha: '25/10/2026 09:20', tipo: 'Salida', clase: 'rojo', sku: 'AD-TFIT-M', producto: 'Polera de Entrenamiento TechFit', cantidad: '-1 ud', responsable: 'Carlos Mendoza', origen: 'Boleta #4321' },
    { id: 'MV-9280', fecha: '24/10/2026 15:30', tipo: 'Ingreso', clase: 'verde', sku: 'AD-TFIT-M', producto: 'Polera de Entrenamiento TechFit', cantidad: '+10 uds', responsable: 'Carlos Mendoza', origen: 'Guía #8911' },
  ];
}