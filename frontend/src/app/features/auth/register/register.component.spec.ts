import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { FormGroup } from '@angular/forms';
import { RegisterComponent } from './register.component';

describe('RegisterComponent form validation', () => {
  function createForm(): FormGroup {
    const fixture = TestBed.createComponent(RegisterComponent);
    return (fixture.componentInstance as unknown as { form: FormGroup }).form;
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
      ],
    });
  });

  it('is invalid when empty', () => {
    expect(createForm().invalid).toBeTrue();
  });

  it('rejects a weak password', () => {
    const form = createForm();
    form.patchValue({
      firstName: 'Max',
      lastName: 'M',
      email: 'a@b.de',
      password: 'weak',
      confirmPassword: 'weak',
    });
    expect(form.controls['password'].hasError('pattern')).toBeTrue();
    expect(form.invalid).toBeTrue();
  });

  it('accepts a policy-compliant password', () => {
    const form = createForm();
    form.patchValue({
      firstName: 'Max',
      lastName: 'Muster',
      email: 'max@b.de',
      password: 'Str0ng!Passw0rd',
      confirmPassword: 'Str0ng!Passw0rd',
    });
    expect(form.valid).toBeTrue();
  });

  it('rejects different passwords', () => {
    const form = createForm();
    form.patchValue({
      firstName: 'Max',
      lastName: 'Muster',
      email: 'max@b.de',
      password: 'Str0ng!Passw0rd',
      confirmPassword: 'Different!Passw0rd1',
    });
    expect(form.hasError('passwordMismatch')).toBeTrue();
    expect(form.invalid).toBeTrue();
  });
});
