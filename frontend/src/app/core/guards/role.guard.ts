import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';
import { UserRole } from '../auth/auth.models';

/**
 * Erzeugt einen Guard, der eine bestimmte Rolle voraussetzt. Nicht berechtigte
 * Nutzer werden auf das Dashboard (bzw. Login) zurückgeleitet.
 */
export function roleGuard(required: UserRole): CanActivateFn {
  return () => {
    const store = inject(AuthStore);
    const router = inject(Router);

    if (!store.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }
    if (store.user()?.role === required) {
      return true;
    }
    return router.createUrlTree(['/app/dashboard']);
  };
}
