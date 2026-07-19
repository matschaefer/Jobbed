import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ApplicationApi } from '../../core/api/application.api';
import {
  Activity,
  ApplicationDetail,
  ApplicationStatus,
} from '../../core/models/application.model';
import {
  STATUS_OPTIONS,
  priorityLabel,
  statusLabel,
  workModelLabel,
} from '../../core/models/domain-options';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { StatusBadgeComponent } from '../../shared/status-badge.component';
import { DocumentPanelComponent } from '../documents/document-panel.component';

const CONFIRM_STATUSES: ApplicationStatus[] = ['REJECTED', 'WITHDRAWN'];

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatProgressBarModule,
    StatusBadgeComponent,
    DocumentPanelComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './application-detail.component.html',
  styleUrl: './application-detail.component.scss',
})
export class ApplicationDetailComponent implements OnInit {
  private readonly api = inject(ApplicationApi);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  readonly id = input.required<string>();

  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly priorityLabel = priorityLabel;
  protected readonly workModelLabel = workModelLabel;
  protected readonly statusLabel = statusLabel;

  protected readonly loading = signal(true);
  protected readonly detail = signal<ApplicationDetail | null>(null);
  protected readonly activities = signal<Activity[]>([]);
  protected readonly statusControl = new FormControl<ApplicationStatus>('SAVED', {
    nonNullable: true,
  });

  ngOnInit(): void {
    this.load();
  }

  applyStatus(): void {
    const detail = this.detail();
    const next = this.statusControl.value;
    if (!detail || next === detail.currentStatus) {
      return;
    }
    if (
      CONFIRM_STATUSES.includes(next) &&
      !confirm(`Status wirklich auf "${statusLabel(next)}" setzen?`)
    ) {
      this.statusControl.setValue(detail.currentStatus);
      return;
    }
    this.api.changeStatus(detail.id, next).subscribe({
      next: (updated) => {
        this.detail.set(updated);
        this.notify.success('Status aktualisiert.');
        this.reloadActivities();
      },
      error: (err: HttpErrorResponse) => {
        this.statusControl.setValue(detail.currentStatus);
        this.notify.error(extractErrorMessage(err));
      },
    });
  }

  remove(): void {
    const detail = this.detail();
    if (!detail || !confirm('Diese Bewerbung wirklich löschen?')) {
      return;
    }
    this.api.delete(detail.id).subscribe({
      next: () => {
        this.notify.success('Bewerbung gelöscht.');
        void this.router.navigate(['/app/applications']);
      },
      error: (err: HttpErrorResponse) => this.notify.error(extractErrorMessage(err)),
    });
  }

  private load(): void {
    this.loading.set(true);
    this.api.get(this.id()).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.statusControl.setValue(detail.currentStatus);
        this.loading.set(false);
        this.reloadActivities();
      },
      error: () => this.loading.set(false),
    });
  }

  protected reloadActivities(): void {
    this.api.activities(this.id()).subscribe((page) => this.activities.set(page.content));
  }
}
