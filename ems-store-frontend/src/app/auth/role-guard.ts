import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  try {
    const user = JSON.parse(sessionStorage.getItem('emsStoreUser') ?? '{}') as { rol?: string };
    const allowed = route.data['roles'] as string[] | undefined;
    if (user.rol && (!allowed || allowed.includes(user.rol))) return true;
  } catch { /* sesión inválida */ }
  return router.parseUrl('/dashboard');
};
