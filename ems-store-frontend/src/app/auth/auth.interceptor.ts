import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('emsStoreToken') : null;
  if (!token || request.url.includes('/api/auth/login')) return next(request);
  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
