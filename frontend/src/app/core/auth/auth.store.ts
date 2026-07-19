import { Injectable, computed, signal } from '@angular/core';
import { AuthResponse, AuthStatus, AuthUser } from './auth.models';

/**
 * Signal-basierter Auth-State. Hält den aktuellen Nutzer und das Access-Token
 * ausschließlich im Speicher (bewusst kein localStorage – Schutz vor XSS-
 * Token-Diebstahl; das Refresh-Token liegt als HttpOnly-Cookie im Browser).
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly accessTokenSig = signal<string | null>(null);
  private readonly userSig = signal<AuthUser | null>(null);
  private readonly statusSig = signal<AuthStatus>('unknown');

  readonly user = this.userSig.asReadonly();
  readonly status = this.statusSig.asReadonly();
  readonly isAuthenticated = computed(() => this.statusSig() === 'authenticated');
  readonly isAdmin = computed(() => this.userSig()?.role === 'ADMIN');
  readonly displayName = computed(() => {
    const u = this.userSig();
    return u ? `${u.firstName} ${u.lastName}`.trim() : '';
  });

  accessToken(): string | null {
    return this.accessTokenSig();
  }

  setSession(response: AuthResponse): void {
    this.accessTokenSig.set(response.accessToken);
    this.userSig.set(response.user);
    this.statusSig.set('authenticated');
  }

  clear(): void {
    this.accessTokenSig.set(null);
    this.userSig.set(null);
    this.statusSig.set('anonymous');
  }
}
