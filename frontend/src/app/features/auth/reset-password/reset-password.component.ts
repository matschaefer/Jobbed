import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
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

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,128}$/;

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('newPassword')?.value;
  const confirm = control.get('confirm')?.value;
  return password && confirm && password !== confirm ? { mismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
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
  templateUrl: './reset-password.component.html',
  styleUrl: '../auth-form.scss',
})
export class ResetPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  readonly token = input<string>('');
  protected readonly hasToken = computed(() => this.token().length > 0);
  protected readonly submitting = signal(false);
  protected readonly done = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group(
    {
      newPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirm: ['', [Validators.required]],
    },
    { validators: passwordsMatch },
  );

  submit(): void {
    if (this.form.invalid || this.submitting() || !this.hasToken()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.auth.resetPassword(this.token(), this.form.getRawValue().newPassword).subscribe({
      next: () => this.done.set(true),
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(extractErrorMessage(error));
      },
    });
  }
}
