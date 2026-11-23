import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-recuperar-contrasena',
  imports: [FormsModule, RouterLink],
  templateUrl: './recuperar-contrasena.html',
  styleUrl: './recuperar-contrasena.scss',
})
export class RecuperarContrasena {
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  token = this.route.snapshot.queryParamMap.get('token') ?? '';
  correo = '';
  nueva = '';
  confirmar = '';
  cargando = false;
  error = '';
  mensaje = '';

  solicitar(): void {
    this.error = '';
    this.mensaje = '';
    this.cargando = true;
    this.auth.solicitarRecuperacion(this.correo.trim()).pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: r => this.mensaje = r.mensaje,
        error: e => this.error = e?.error?.message || e?.error?.detail || 'No se pudo procesar la solicitud. Revisa la conexión e inténtalo de nuevo.',
      });
  }

  guardar(): void {
    this.error = '';
    this.mensaje = '';
    if (this.nueva !== this.confirmar) { this.error = 'Las contraseñas no coinciden.'; return; }
    this.cargando = true;
    this.auth.restablecerContrasena(this.token, this.nueva).pipe(finalize(() => this.cargando = false))
      .subscribe({ next: r => this.mensaje = r.mensaje, error: e => this.error = e?.error?.detail || e?.error?.message || 'No se pudo restablecer la contraseña.' });
  }
}
