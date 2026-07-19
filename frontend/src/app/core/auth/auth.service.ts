import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  AuthUser,
  LoginPayload,
  MessageResponse,
  RegisterPayload,
} from './auth.models';
import { AuthStore } from './auth.store';

/**
 * Kapselt alle Auth-HTTP-Aufrufe und aktualisiert den {@link AuthStore}.
 * Cookie-gestützte Endpunkte (login/refresh/logout) laufen mit
 * {@code withCredentials}, damit das Refresh-Cookie übertragen wird.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly store = inject(AuthStore);
  private readonly base = `${environment.apiBaseUrl}/auth`;

  /** Laufender Refresh-Aufruf für Single-Flight (verhindert parallele Refreshes). */
  private refreshInFlight$: Observable<boolean> | null = null;

  register(payload: RegisterPayload): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.base}/register`, payload);
  }

  login(payload: LoginPayload): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.base}/login`, payload, { withCredentials: true })
      .pipe(tap((res) => this.store.setSession(res)));
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.base}/logout`, {}, { withCredentials: true }).pipe(
      catchError(() => of(void 0)),
      finalize(() => this.store.clear()),
    );
  }

  verifyEmail(token: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/verify-email`, { token });
  }

  forgotPassword(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/reset-password`, { token, newPassword });
  }

  /**
   * Erneuert das Access-Token über das Refresh-Cookie. Single-Flight: parallele
   * Aufrufer teilen sich denselben laufenden Request. Liefert {@code false},
   * wenn keine gültige Sitzung besteht.
   */
  refresh(): Observable<boolean> {
    if (!this.refreshInFlight$) {
      this.refreshInFlight$ = this.http
        .post<AuthResponse>(`${this.base}/refresh`, {}, { withCredentials: true })
        .pipe(
          tap((res) => this.store.setSession(res)),
          map(() => true),
          catchError(() => {
            this.store.clear();
            return of(false);
          }),
          finalize(() => (this.refreshInFlight$ = null)),
          shareReplay(1),
        );
    }
    return this.refreshInFlight$;
  }
}
