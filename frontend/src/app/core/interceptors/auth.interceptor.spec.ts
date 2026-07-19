import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from '../auth/auth.store';
import { AuthService } from '../auth/auth.service';
import { AuthResponse } from '../auth/auth.models';

function session(token: string): AuthResponse {
  return {
    accessToken: token,
    tokenType: 'Bearer',
    expiresIn: 900,
    user: {
      id: '1',
      firstName: 'A',
      lastName: 'B',
      email: 'a@b.de',
      role: 'USER',
      emailVerified: true,
    },
  };
}

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let store: AuthStore;
  let refreshSpy: jasmine.Spy;

  beforeEach(() => {
    refreshSpy = jasmine.createSpy('refresh').and.returnValue(of(false));
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthStore,
        { provide: AuthService, useValue: { refresh: refreshSpy } },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(AuthStore);
  });

  afterEach(() => httpMock.verify());

  it('attaches the bearer token to API requests', () => {
    store.setSession(session('token-abc'));
    http.get('/api/v1/applications').subscribe();

    const req = httpMock.expectOne('/api/v1/applications');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-abc');
    req.flush({});
  });

  it('does not attach a token to public auth endpoints', () => {
    store.setSession(session('token-abc'));
    http.post('/api/v1/auth/login', {}).subscribe();

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('refreshes once on 401 and retries with the new token', () => {
    store.setSession(session('old-token'));
    refreshSpy.and.callFake(() => {
      store.setSession(session('new-token'));
      return of(true);
    });

    let result: unknown;
    http.get('/api/v1/protected').subscribe((r) => (result = r));

    const first = httpMock.expectOne('/api/v1/protected');
    expect(first.request.headers.get('Authorization')).toBe('Bearer old-token');
    first.flush({ error: 'expired' }, { status: 401, statusText: 'Unauthorized' });

    const retry = httpMock.expectOne('/api/v1/protected');
    expect(retry.request.headers.get('Authorization')).toBe('Bearer new-token');
    retry.flush({ ok: true });

    expect(refreshSpy).toHaveBeenCalledTimes(1);
    expect(result).toEqual({ ok: true });
  });
});
