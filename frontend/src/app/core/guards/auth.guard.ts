import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';

/** Lässt nur authentifizierte Nutzer passieren; sonst Weiterleitung zum Login. */
export const authGuard: CanActivateFn = (_route, state) => {
  const store = inject(AuthStore);
  const router = inject(Router);

  if (store.isAuthenticated()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
};
