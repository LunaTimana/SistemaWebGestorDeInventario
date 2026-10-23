import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por servicios con HttpClient.
@Component({
  selector: 'app-registrar-salida',
  imports: [RouterLink],
  templateUrl: './registrar-salida.html',
  styleUrl: './registrar-salida.scss',
})
export class RegistrarSalida {
  productos = [
    'Zapatillas Pegasus 40 Zoom • NK-PEG40-10',
    'Polera de Entrenamiento TechFit • AD-TFIT-M',
    'Cortavientos Storm Impermeable • UA-STORM-L',
    'Shorts de Running Dri-FIT Flex • NK-DRIT-S',
    'Zapatillas Deviate Nitro Elite • PM-DEVI-43',
  ];

  tiposSalida = ['Venta', 'Devolución a proveedor', 'Merma o pérdida', 'Ajuste de inventario'];

  responsables = ['Carlos Mendoza', 'Marcio Morales'];

  fecha = '26/10/2026';

  // Ejemplo del prototipo: se retiran 18 de un stock de 15
  stockActual = 15;
  cantidad = 18;
  proyectado = this.stockActual - this.cantidad;
  hayError = this.proyectado < 0;
}