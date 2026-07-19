import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Zentriertes Card-Layout für alle Auth-Seiten (Login, Registrierung, …). */
@Component({
  selector: 'app-auth-shell',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="grid min-h-screen place-items-center bg-bg px-4 py-10">
      <div class="w-full max-w-[420px]">
        <div class="rounded-2xl border border-border bg-surface p-8 shadow-panel">
          <a routerLink="/" class="mb-6 inline-flex items-center gap-2 font-extrabold text-text-hi">
            <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-brand-fg">
              <span class="material-icons-round text-[20px]">work</span>
            </span>
            Jobbed
          </a>
          <h1 class="m-0 text-2xl font-bold text-text-hi">{{ title }}</h1>
          @if (subtitle) {
            <p class="m-0 mt-1 text-sm text-muted">{{ subtitle }}</p>
          }
          <div class="mt-6">
            <ng-content />
          </div>
        </div>
      </div>
    </div>
  `,
})
export class AuthShellComponent {
  @Input({ required: true }) title!: string;
  @Input() subtitle?: string;
}
