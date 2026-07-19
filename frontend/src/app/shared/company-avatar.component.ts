import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

const PALETTE = [
  '#7c3aed', // violet (Twitch-like)
  '#f5b301', // amber (Snapchat-like)
  '#1db954', // green (Spotify-like)
  '#2d8cff', // blue (Zoom-like)
  '#ef4444', // red
  '#ec4899', // pink
  '#06b6d4', // cyan
  '#f97316', // orange
];

/** Farbiger Avatar-Kreis mit Firmen-Initialen (deterministische Farbe je Name). */
@Component({
  selector: 'app-company-avatar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span
      class="avatar"
      [style.background]="color()"
      [style.width.px]="size()"
      [style.height.px]="size()"
      [style.fontSize.px]="size() * 0.42"
      [attr.aria-hidden]="true"
    >
      {{ initials() }}
    </span>
  `,
  styles: [
    `
      .avatar {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        color: #fff;
        font-weight: 700;
        flex: 0 0 auto;
        letter-spacing: 0.5px;
        text-transform: uppercase;
      }
    `,
  ],
})
export class CompanyAvatarComponent {
  readonly name = input<string>('');
  readonly size = input<number>(40);

  protected readonly initials = computed(() => {
    const parts = this.name().trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '?';
    if (parts.length === 1) return parts[0].slice(0, 2);
    return (parts[0][0] + parts[1][0]).slice(0, 2);
  });

  protected readonly color = computed(() => {
    const name = this.name();
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = (hash * 31 + name.charCodeAt(i)) >>> 0;
    }
    return PALETTE[hash % PALETTE.length];
  });
}
