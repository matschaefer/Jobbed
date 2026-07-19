import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';
import { extractErrorMessage } from '../../../core/interceptors/error.interceptor';
import { AuthShellComponent } from '../auth-shell.component';

/** Muss der Backend-Passwortrichtlinie entsprechen (siehe PasswordPolicy.java). */
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,128}$/;

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmation = control.get('confirmPassword')?.value;
  return password && confirmation && password !== confirmation ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    AuthShellComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './register.component.html',
  styleUrl: '../auth-form.scss',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  protected readonly submitting = signal(false);
  protected readonly registered = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly mailpitUrl = ['localhost', '127.0.0.1'].includes(globalThis.location.hostname)
    ? 'http://localhost:8025'
    : null;

  protected readonly form = this.fb.nonNullable.group(
    {
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatch },
  );

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);

    const values = this.form.getRawValue();
    const payload = {
      firstName: values.firstName,
      lastName: values.lastName,
      email: values.email,
      password: values.password,
    };
    this.auth.register(payload).subscribe({
      next: () => this.registered.set(true),
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(extractErrorMessage(error));
      },
    });
  }
}
