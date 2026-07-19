import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccountApi } from '../../core/api/account.api';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './settings.component.html',
})
export class SettingsComponent {
  protected readonly store = inject(AuthStore);
  private readonly fb = inject(FormBuilder);
  private readonly accountApi = inject(AccountApi);
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    password: ['', Validators.required],
    confirmText: ['', Validators.required],
  });

  protected deleteAccount(): void {
    if (this.store.isDemo()) {
      this.notifications.info('Demo-Modus: Aenderungen sind deaktiviert.');
      return;
    }
    if (this.form.invalid || this.form.controls.confirmText.value !== 'LOESCHEN' || this.submitting()) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Bitte Passwort eingeben und LOESCHEN bestaetigen.');
      return;
    }
    if (!confirm('Konto und alle gespeicherten Daten dauerhaft loeschen?')) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.accountApi.deleteAccount(this.form.controls.password.value).subscribe({
      next: () => {
        this.notifications.success('Konto wurde geloescht.');
        this.auth.logout().subscribe(() => void this.router.navigateByUrl('/'));
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(extractErrorMessage(error));
      },
    });
  }
}
