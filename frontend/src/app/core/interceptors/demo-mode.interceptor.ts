import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { AuthStore } from '../auth/auth.store';
import { NotificationService } from '../notifications/notification.service';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);
const AUTH_ALLOWLIST = ['/auth/demo', '/auth/refresh', '/auth/logout'];

export const demoModeInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(AuthStore);
  const notifications = inject(NotificationService);

  if (store.isDemo() && !SAFE_METHODS.has(req.method) && !AUTH_ALLOWLIST.some((path) => req.url.endsWith(path))) {
    const message = 'Demo-Modus: Aenderungen sind deaktiviert.';
    notifications.info(message);
    return throwError(
      () =>
        new HttpErrorResponse({
          status: 403,
          statusText: 'Forbidden',
          url: req.url,
          error: { message },
        }),
    );
  }

  return next(req);
};
