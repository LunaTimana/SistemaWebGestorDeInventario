import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la sesión, vendrán del usuario que inició sesión.
@Component({
  selector: 'app-configuracion-cuenta',
  imports: [RouterLink],
  templateUrl: './configuracion-cuenta.html',
  styleUrl: './configuracion-cuenta.scss',
})
export class ConfiguracionCuenta {
  usuario = {
    nombre: 'Marcio',
    apellido: 'Morales',
    iniciales: 'MM',
    rol: 'Admin de Tienda',
    sucursal: 'Sucursal Megatienda Oriente',
  };
}