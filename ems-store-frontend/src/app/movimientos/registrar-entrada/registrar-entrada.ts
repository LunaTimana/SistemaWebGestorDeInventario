import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la API, se reemplazan por servicios con HttpClient.
@Component({
  selector: 'app-registrar-entrada',
  imports: [RouterLink],
  templateUrl: './registrar-entrada.html',
  styleUrl: './registrar-entrada.scss',
})
export class RegistrarEntrada {
  productos = [
    'Zapatillas Pegasus 40 Zoom • NK-PEG40-10',
    'Polera de Entrenamiento TechFit • AD-TFIT-M',
    'Cortavientos Storm Impermeable • UA-STORM-L',
    'Shorts de Running Dri-FIT Flex • NK-DRIT-S',
    'Zapatillas Deviate Nitro Elite • PM-DEVI-43',
  ];

  proveedores = ['Nike Distribuciones S.A.', 'UA Distribuidora', 'Distribuidora Adidas', 'Distribuidora Puma'];

  responsables = ['Carlos Mendoza', 'Marcio Morales'];

  fecha = '26/10/2026';

  resumen = { actual: '15 unidades', proyectado: '60 unidades' };
}