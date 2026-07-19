import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { AuthStore } from '../auth/auth.store';

/** Lässt nur authentifizierte Nutzer passieren; sonst Weiterleitung zum Login. */
export const authGuard: CanActivateFn = (_route, state) => {
  const store = inject(AuthStore);
  const auth = inject(AuthService);
  const router = inject(Router);
  const loginTree = () => router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });

  if (store.isAuthenticated()) {
    return true;
  }
  if (store.status() === 'unknown') {
    return auth.refresh().pipe(
      map((authenticated) => (authenticated ? true : loginTree())),
      catchError(() => of(loginTree())),
    );
  }
  return loginTree();
};
