import { AuthStore } from './auth.store';
import { AuthResponse } from './auth.models';

describe('AuthStore', () => {
  let store: AuthStore;

  const response: AuthResponse = {
    accessToken: 'token-123',
    tokenType: 'Bearer',
    expiresIn: 900,
    user: {
      id: 'u1',
      firstName: 'Max',
      lastName: 'Muster',
      email: 'max@b.de',
      role: 'USER',
      emailVerified: true,
    },
  };

  beforeEach(() => {
    store = new AuthStore();
  });

  it('starts in unknown/anonymous state', () => {
    expect(store.status()).toBe('unknown');
    expect(store.isAuthenticated()).toBeFalse();
    expect(store.accessToken()).toBeNull();
  });

  it('sets session on setSession', () => {
    store.setSession(response);
    expect(store.isAuthenticated()).toBeTrue();
    expect(store.accessToken()).toBe('token-123');
    expect(store.user()?.email).toBe('max@b.de');
    expect(store.displayName()).toBe('Max Muster');
    expect(store.isAdmin()).toBeFalse();
  });

  it('clears session', () => {
    store.setSession(response);
    store.clear();
    expect(store.status()).toBe('anonymous');
    expect(store.isAuthenticated()).toBeFalse();
    expect(store.accessToken()).toBeNull();
    expect(store.user()).toBeNull();
  });

  it('detects admin role', () => {
    store.setSession({ ...response, user: { ...response.user, role: 'ADMIN' } });
    expect(store.isAdmin()).toBeTrue();
  });
});
