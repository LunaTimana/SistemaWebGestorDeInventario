import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

// Datos de ejemplo tomados del prototipo de Figma.
// Cuando exista la sesión, vendrán del usuario que inició sesión.
@Component({
  selector: 'app-configuracion-cuenta',
  imports: [RouterLink],
  templateUrl: './configuracion-cuenta.html',
  styleUrl: './configuracion-cuenta.scss',
})
export class ConfiguracionCuenta {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  usuario = {
    nombre: 'Marcio',
    apellido: 'Morales',
    iniciales: 'MM',
    rol: 'Admin de Tienda',
    sucursal: 'Sucursal Megatienda Oriente',
  };

  cerrarSesion(): void {
    this.authService.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}