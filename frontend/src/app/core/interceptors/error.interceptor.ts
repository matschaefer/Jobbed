import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../auth/auth.models';
import { NotificationService } from '../notifications/notification.service';

/** Extrahiert die Backend-Fehlermeldung oder liefert einen Fallback je Status. */
export function extractErrorMessage(error: HttpErrorResponse): string {
  const apiError = error.error as ApiError | undefined;
  if (apiError?.message) {
    return apiError.message;
  }
  switch (error.status) {
    case 0:
      return 'Keine Verbindung zum Server.';
    case 403:
      return 'Zugriff verweigert.';
    case 404:
      return 'Die angeforderte Ressource wurde nicht gefunden.';
    case 409:
      return 'Es besteht ein Konflikt mit dem aktuellen Zustand.';
    case 422:
      return 'Die Anfrage konnte nicht verarbeitet werden.';
    case 429:
      return 'Zu viele Anfragen. Bitte später erneut versuchen.';
    default:
      return 'Ein unerwarteter Fehler ist aufgetreten.';
  }
}

/**
 * Globales Fehler-Mapping: zeigt Toasts für Serverfehler an. 401 wird bewusst
 * dem {@link authInterceptor} überlassen (Token-Refresh); Validierungsfehler
 * (400/422) werden i. d. R. im Formular selbst dargestellt.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        const silent = error.status === 401 || error.status === 400 || error.status === 422;
        if (!silent) {
          notifications.error(extractErrorMessage(error));
        }
      }
      return throwError(() => error);
    }),
  );
};
