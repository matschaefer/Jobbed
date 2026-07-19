import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

interface Feature {
  readonly icon: string;
  readonly title: string;
  readonly description: string;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly demoSubmitting = signal(false);

  protected readonly features: readonly Feature[] = [
    {
      icon: 'view_kanban',
      title: 'Ein Board, volle Klarheit',
      description: 'Von gespeichert bis Angebot: Jede Bewerbung bleibt sichtbar und in Bewegung.',
    },
    {
      icon: 'insights',
      title: 'Fortschritt, der motiviert',
      description: 'Verstehe deine Erfolgsquote, Quellen und nächsten sinnvollen Schritte.',
    },
    {
      icon: 'event',
      title: 'Keinen Termin verpassen',
      description: 'Interviews, Follow-ups und Erinnerungen landen gemeinsam in deinem Kalender.',
    },
    {
      icon: 'description',
      title: 'Besser bewerben mit KI',
      description: 'Analysiere Stellenanzeigen und erstelle passgenaue Lebenslauf-Entwürfe.',
    },
  ];

  protected readonly stats = [
    { value: '12', label: 'klare Statusschritte' },
    { value: '1', label: 'Ort für den ganzen Prozess' },
    { value: '0', label: 'vergessene Follow-ups' },
    { value: '100%', label: 'deine Daten, dein Konto' },
  ] as const;

  protected demoLogin(): void {
    if (this.demoSubmitting()) {
      return;
    }
    this.demoSubmitting.set(true);
    this.auth.demoLogin().subscribe({
      next: () => void this.router.navigateByUrl('/app/dashboard'),
      error: () => {
        this.demoSubmitting.set(false);
        void this.router.navigateByUrl('/login');
      },
    });
  }
}
