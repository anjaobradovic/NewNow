import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Skip adding token for public auth endpoints
  const isAuthEndpoint = req.url.includes('/api/auth/');
  const isPublicEndpoint =
    req.url.includes('/api/events/today') || req.url.includes('/api/locations/popular');

  if (token && !isAuthEndpoint && !isPublicEndpoint) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`),
    });
    return next(cloned);
  }

  return next(req);
};
