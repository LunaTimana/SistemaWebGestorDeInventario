import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const usuarioGuardado = sessionStorage.getItem('emsStoreUser');
  const tokenGuardado = sessionStorage.getItem('emsStoreToken');
  if (!usuarioGuardado || !tokenGuardado) return router.parseUrl('/login');

  try {
    const usuario = JSON.parse(usuarioGuardado) as { id?: number; correo?: string };
    if (usuario.id && usuario.correo) return true;
  } catch {
    // Sesión inválida: se elimina abajo.
  }

  sessionStorage.removeItem('emsStoreUser');
  sessionStorage.removeItem('emsStoreToken');
  return router.parseUrl('/login');
};
