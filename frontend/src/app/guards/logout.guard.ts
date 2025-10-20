import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Guard that performs logout action and redirects home
export const logoutGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  authService.logout(false).subscribe({
    next: () => {
      router.navigate(['/']);
    },
    error: () => {
      authService.clearSession();
      router.navigate(['/']);
    },
  });

  return false; // navigation will be redirected
};
