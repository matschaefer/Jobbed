import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="not-found">
      <p class="not-found__code">404</p>
      <h1>Seite nicht gefunden</h1>
      <p>Die angeforderte Seite existiert nicht oder wurde verschoben.</p>
      <a mat-flat-button color="primary" routerLink="/">Zur Startseite</a>
    </section>
  `,
  styles: [
    `
      .not-found {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.75rem;
        text-align: center;
        padding: 2rem;
      }
      .not-found__code {
        font-size: 4rem;
        font-weight: 700;
        margin: 0;
        color: var(--jt-primary);
      }
    `,
  ],
})
export class NotFoundComponent {}
