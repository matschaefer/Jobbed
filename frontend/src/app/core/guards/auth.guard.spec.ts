import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { RouterModule } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthStore } from '../auth/auth.store';

describe('authGuard', () => {
  let store: AuthStore;

  function runGuard(): boolean | UrlTree {
    return TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/app/dashboard' } as never),
    ) as boolean | UrlTree;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      providers: [AuthStore],
    });
    store = TestBed.inject(AuthStore);
  });

  it('allows access when authenticated', () => {
    store.setSession({
      accessToken: 't',
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
    });
    expect(runGuard()).toBeTrue();
  });

  it('redirects to /login when not authenticated', () => {
    const result = runGuard();
    expect(result instanceof UrlTree).toBeTrue();
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toContain('/login');
  });
});
