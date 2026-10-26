import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  imports: [CommonModule, FormsModule],
  selector: 'app-login',
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  correo = '';
  contrasena = '';
  error = '';
  cargando = false;
  mostrarContrasena = false;

  ingresar(): void {
    if (this.cargando) return;
    this.error = '';
    this.cargando = true;
    this.auth.login(this.correo.trim(), this.contrasena).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
        const destinoLocal = returnUrl.startsWith('/') && !returnUrl.startsWith('//');
        void this.router.navigateByUrl(destinoLocal ? returnUrl : '/dashboard');
      },
      error: () => {
        this.error = 'Correo o contraseña incorrectos, o cuenta inactiva.';
        this.cargando = false;
      },
    });
  }
}
