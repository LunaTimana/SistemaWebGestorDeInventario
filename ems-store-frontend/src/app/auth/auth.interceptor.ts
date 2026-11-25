import { ApplicationRef, inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { finalize, timeout } from 'rxjs';
import { API_URL } from '../api-url';

/**
 * Angular 22 trabaja sin Zone.js por defecto. Los componentes usan suscripciones
 * RxJS tradicionales que actualizan propiedades normales; por eso, sin notificar
 * al ciclo de renderizado, las tablas y botones parecían actualizarse recién al
 * hacer otro clic. Este interceptor dispara una detección después de cada petición
 * HTTP y corta las peticiones que se quedan colgadas.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const appRef = inject(ApplicationRef);
  const token = typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('emsStoreToken') : null;
  const isBackendRequest = request.url.startsWith(`${API_URL}/`);
  const isLoginRequest = request.url === `${API_URL}/auth/login`;
  const authenticatedRequest = token && isBackendRequest && !isLoginRequest
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(
    timeout({ first: 15000 }),
    finalize(() => {
      // Las respuestas de HttpClient son asíncronas; en modo zoneless hay que
      // notificar explícitamente que cambió el estado de los componentes.
      try { appRef.tick(); } catch (error) { console.debug('Render posterior a HTTP:', error); }
    })
  );
};
