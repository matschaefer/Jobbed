import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';
import { AuthShellComponent } from '../auth-shell.component';

type VerifyState = 'verifying' | 'success' | 'error';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    AuthShellComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-auth-shell title="E-Mail-Bestätigung">
      <div class="auth-success">
        @switch (state()) {
          @case ('verifying') {
            <mat-progress-spinner diameter="40" mode="indeterminate" />
            <p>Deine E-Mail-Adresse wird bestätigt …</p>
          }
          @case ('success') {
            <mat-icon aria-hidden="true">check_circle</mat-icon>
            <h2>E-Mail bestätigt</h2>
            <p>Du kannst dich jetzt anmelden.</p>
            <a mat-flat-button color="primary" routerLink="/login">Zur Anmeldung</a>
          }
          @case ('error') {
            <mat-icon aria-hidden="true" class="auth-icon-error">error</mat-icon>
            <h2>Bestätigung fehlgeschlagen</h2>
            <p>Der Link ist ungültig oder abgelaufen.</p>
            <a mat-stroked-button routerLink="/login">Zur Anmeldung</a>
          }
        }
      </div>
    </app-auth-shell>
  `,
  styleUrl: '../auth-form.scss',
})
export class VerifyEmailComponent implements OnInit {
  private readonly auth = inject(AuthService);

  /** Token aus dem Query-Parameter (?token=…) via withComponentInputBinding. */
  readonly token = input<string>('');
  protected readonly state = signal<VerifyState>('verifying');

  ngOnInit(): void {
    const token = this.token();
    if (!token) {
      this.state.set('error');
      return;
    }
    this.auth.verifyEmail(token).subscribe({
      next: () => this.state.set('success'),
      error: () => this.state.set('error'),
    });
  }
}
