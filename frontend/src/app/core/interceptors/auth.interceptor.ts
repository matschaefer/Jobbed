import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { AuthStore } from '../auth/auth.store';

const PUBLIC_AUTH_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/verify-email',
  '/auth/forgot-password',
  '/auth/reset-password',
];

function isApiRequest(req: HttpRequest<unknown>): boolean {
  return req.url.startsWith(environment.apiBaseUrl) || req.url.startsWith('/api/');
}

function isPublicAuthRequest(req: HttpRequest<unknown>): boolean {
  return PUBLIC_AUTH_ENDPOINTS.some((endpoint) => req.url.includes(endpoint));
}

function withToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

/**
 * Hängt das Access-Token an API-Requests an und erneuert es bei 401 automatisch
 * (Single-Flight über {@link AuthService.refresh}). Schlägt der Refresh fehl,
 * wird die Sitzung verworfen und zur Anmeldung weitergeleitet.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(AuthStore);
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = store.accessToken();
  const authReq =
    token && isApiRequest(req) && !isPublicAuthRequest(req) ? withToken(req, token) : req;

  return next(authReq).pipe(
    catchError((error: unknown) => {
      const is401 = error instanceof HttpErrorResponse && error.status === 401;
      if (!is401 || !isApiRequest(req) || isPublicAuthRequest(req)) {
        return throwError(() => error);
      }
      // Access-Token abgelaufen -> einmalig erneuern und Request wiederholen.
      return auth.refresh().pipe(
        switchMap((ok) => {
          const newToken = store.accessToken();
          if (ok && newToken) {
            return next(withToken(req, newToken));
          }
          void router.navigate(['/login'], { queryParams: { redirect: router.url } });
          return throwError(() => error);
        }),
      );
    }),
  );
};
