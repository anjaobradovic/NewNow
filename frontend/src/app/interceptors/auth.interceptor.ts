import { HttpInterceptorFn, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  const isAuthEndpoint = req.url.includes('/api/auth/');
  const isPublicEndpoint =
    req.url.includes('/api/events/today') || req.url.includes('/api/locations/popular');

  let headers = req.headers as HttpHeaders;
  if (token && !isAuthEndpoint && !isPublicEndpoint) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  return next(req.clone({ headers })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthEndpoint) {
        // Attempt silent refresh once
        const refreshToken = authService.getRefreshToken();
        if (!refreshToken) {
          authService.clearSession();
          router.navigate(['/auth/login']);
          return throwError(() => error);
        }
        return authService.refresh().pipe(
          switchMap(() => {
            const newToken = authService.getToken();
            const retryHeaders = req.headers.set('Authorization', `Bearer ${newToken}`);
            return next(req.clone({ headers: retryHeaders }));
          }),
          catchError((refreshErr) => {
            authService.clearSession();
            router.navigate(['/auth/login']);
            return throwError(() => refreshErr);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
