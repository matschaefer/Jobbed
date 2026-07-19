import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageEvent, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ApplicationApi } from '../../core/api/application.api';
import { ApplicationStatus, ApplicationSummary } from '../../core/models/application.model';
import { STATUS_OPTIONS, priorityLabel } from '../../core/models/domain-options';
import { Page } from '../../core/models/page.model';
import { StatusBadgeComponent } from '../../shared/status-badge.component';
import { CompanyAvatarComponent } from '../../shared/company-avatar.component';

@Component({
  selector: 'app-application-list',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatPaginatorModule,
    MatProgressBarModule,
    StatusBadgeComponent,
    CompanyAvatarComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './application-list.component.html',
  styleUrl: './application-list.component.scss',
})
export class ApplicationListComponent implements OnInit {
  private readonly api = inject(ApplicationApi);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly priorityLabel = priorityLabel;
  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly statusFilter = new FormControl<ApplicationStatus | ''>('', { nonNullable: true });

  protected readonly loading = signal(false);
  protected readonly page = signal<Page<ApplicationSummary> | null>(null);
  private pageIndex = 0;
  private pageSize = 20;

  ngOnInit(): void {
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload(true));
    this.statusFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload(true));
    this.reload();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.reload();
  }

  private reload(resetPage = false): void {
    if (resetPage) this.pageIndex = 0;
    this.loading.set(true);
    this.api
      .list({
        page: this.pageIndex,
        size: this.pageSize,
        sort: 'applicationDate,desc',
        query: this.search.value.trim() || undefined,
        status: this.statusFilter.value ? [this.statusFilter.value] : undefined,
      })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }
}
