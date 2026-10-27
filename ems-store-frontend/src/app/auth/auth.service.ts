import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UsuarioAutenticado {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  debeCambiarContrasena: boolean;
  rol: string;
}

export interface LoginResponse {
  mensaje: string;
  usuario: UsuarioAutenticado;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  iniciarSesion(correo: string, contrasena: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { correo, contrasena });
  }

  cerrarSesion(): void {
    sessionStorage.removeItem('emsStoreUser');
    sessionStorage.removeItem('emsStoreToken');
  }

  estaAutenticado(): boolean {
    return sessionStorage.getItem('emsStoreUser') !== null;
  }
}
