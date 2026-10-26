import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginResponse {
  token: string;
  tipo: 'Bearer';
  expiraEnSegundos: number;
  usuario: {
    id: number;
    nombre: string;
    apellido: string;
    correo: string;
    rol: string;
    estado: string;
  };
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenKey = 'ems_access_token';

  login(correo: string, contrasena: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { correo, contrasena }).pipe(
      tap((respuesta) => sessionStorage.setItem(this.tokenKey, respuesta.token)),
    );
  }

  token(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    const token = this.token();
    if (!token) return false;
    try {
      const encoded = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const binary = atob(encoded.padEnd(Math.ceil(encoded.length / 4) * 4, '='));
      const payloadBytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));
      const payload = JSON.parse(new TextDecoder().decode(payloadBytes)) as { exp?: number };
      if (!payload.exp || payload.exp * 1000 <= Date.now()) {
        this.logout(false);
        return false;
      }
      return true;
    } catch {
      this.logout(false);
      return false;
    }
  }

  logout(navigate = true): void {
    sessionStorage.removeItem(this.tokenKey);
    if (navigate) void this.router.navigate(['/login']);
  }
}
