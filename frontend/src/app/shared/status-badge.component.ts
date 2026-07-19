import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { ApplicationStatus } from '../core/models/application.model';
import { statusColorClass, statusLabel } from '../core/models/domain-options';

/**
 * Status-Badge mit farbigem Punkt UND Textlabel (Statusinfo nie rein farbbasiert
 * – Barrierefreiheit).
 */
@Component({
  selector: 'app-status-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="badge" [class]="colorClass()">
      <span class="badge__dot" aria-hidden="true"></span>
      {{ label() }}
    </span>
  `,
  styles: [
    `
      .badge {
        display: inline-flex;
        align-items: center;
        gap: 0.4rem;
        padding: 0.15rem 0.6rem;
        border-radius: 999px;
        font-size: 0.8rem;
        font-weight: 500;
        background: var(--jt-badge-bg, rgba(127, 127, 127, 0.15));
        color: var(--jt-badge-fg, inherit);
        white-space: nowrap;
      }
      .badge__dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: currentColor;
      }
      .status--success {
        --jt-badge-bg: rgba(74, 222, 128, 0.13);
        --jt-badge-fg: #4ade80;
      }
      .status--danger {
        --jt-badge-bg: rgba(251, 113, 133, 0.13);
        --jt-badge-fg: #fb7185;
      }
      .status--interview {
        --jt-badge-bg: rgba(139, 124, 246, 0.15);
        --jt-badge-fg: #a99df9;
      }
      .status--active {
        --jt-badge-bg: rgba(96, 165, 250, 0.13);
        --jt-badge-fg: #60a5fa;
      }
      .status--neutral {
        --jt-badge-bg: rgba(161, 161, 170, 0.12);
        --jt-badge-fg: #a1a1aa;
      }
    `,
  ],
})
export class StatusBadgeComponent {
  readonly status = input.required<ApplicationStatus>();
  protected readonly label = computed(() => statusLabel(this.status()));
  protected readonly colorClass = computed(() => statusColorClass(this.status()));
}
