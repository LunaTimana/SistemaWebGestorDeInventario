import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  correo = '';
  contrasena = '';
  mostrarContrasena = false;
  cargando = false;
  error = '';

  iniciarSesion(): void {
    this.error = '';
    if (!this.correo.trim() || !this.contrasena) {
      this.error = 'Escribe tu correo electrónico y contraseña.';
      return;
    }

    this.cargando = true;
    this.authService.iniciarSesion(this.correo.trim(), this.contrasena)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (respuesta) => {
          sessionStorage.setItem('emsStoreUser', JSON.stringify(respuesta.usuario));
          sessionStorage.setItem('emsStoreToken', respuesta.token);
          void this.router.navigateByUrl('/dashboard');
        },
        error: (error) => {
          if (error.status === 401) {
            this.error = 'Correo o contraseña incorrectos, o usuario inactivo.';
          } else if (error.status === 0) {
            this.error = 'No se pudo conectar con el servidor. Verifica que el backend esté iniciado.';
          } else {
            this.error = 'No se pudo iniciar sesión. Inténtalo nuevamente.';
          }
        },
      });
  }
}
