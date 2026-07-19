import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { AuthStore } from '../core/auth/auth.store';
import { AuthService } from '../core/auth/auth.service';
import { InAppNotificationApi } from '../core/api/in-app-notification.api';
import { InAppNotification } from '../core/models/schedule.model';

interface NavItem {
  readonly path: string;
  readonly label: string;
}

/** Authentifizierte App-Shell mit horizontaler Top-Navigation (Dark Design). */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatMenuModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './shell.component.html',
})
export class ShellComponent {
  protected readonly store = inject(AuthStore);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notificationApi = inject(InAppNotificationApi);
  protected readonly notifications = signal<InAppNotification[]>([]);
  protected readonly unreadCount = signal(0);

  protected readonly navItems: readonly NavItem[] = [
    { path: '/app/dashboard', label: 'Dashboard' },
    { path: '/app/board', label: 'Board' },
    { path: '/app/calendar', label: 'Kalender' },
    { path: '/app/job-analysis', label: 'Analyse' },
    { path: '/app/resume', label: 'Lebenslauf' },
    { path: '/app/applications', label: 'Bewerbungen' },
    { path: '/app/companies', label: 'Unternehmen' },
    { path: '/app/contacts', label: 'Kontakte' },
    { path: '/app/settings', label: 'Einstellungen' },
  ];

  protected readonly initials = computed(() => {
    const u = this.store.user();
    if (!u) return '?';
    return `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase() || '?';
  });

  logout(): void {
    this.auth.logout().subscribe(() => void this.router.navigateByUrl('/login'));
  }

  openNotifications(): void {
    this.notificationApi.list().subscribe((result) => {
      this.notifications.set(result.items);
      this.unreadCount.set(result.unreadCount);
    });
  }

  openNotification(item: InAppNotification): void {
    const navigate = () => item.actionUrl && void this.router.navigateByUrl(item.actionUrl);
    if (item.read) navigate();
    else this.notificationApi.read(item.id).subscribe(() => { item.read = true; this.unreadCount.update(v => Math.max(0, v - 1)); navigate(); });
  }

  markAllRead(): void {
    this.notificationApi.readAll().subscribe(() => {
      this.notifications.update(items => items.map(item => ({ ...item, read: true })));
      this.unreadCount.set(0);
    });
  }
}
