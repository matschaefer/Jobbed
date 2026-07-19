import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CompanyApi } from '../../core/api/company.api';
import { CompanySummary } from '../../core/models/application.model';
import { Page } from '../../core/models/page.model';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { CompanyAvatarComponent } from '../../shared/company-avatar.component';

@Component({
  selector: 'app-company-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatPaginatorModule,
    MatProgressBarModule,
    CompanyAvatarComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './company-list.component.html',
  styleUrl: './company-list.component.scss',
})
export class CompanyListComponent implements OnInit {
  private readonly api = inject(CompanyApi);
  private readonly notify = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly loading = signal(false);
  protected readonly page = signal<Page<CompanySummary> | null>(null);
  private pageIndex = 0;
  private pageSize = 12;

  ngOnInit(): void {
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.reload(true));
    this.reload();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.reload();
  }

  remove(company: CompanySummary): void {
    if (!confirm(`Unternehmen "${company.name}" löschen?`)) {
      return;
    }
    this.api.delete(company.id).subscribe({
      next: () => {
        this.notify.success('Unternehmen gelöscht.');
        this.reload();
      },
      error: (err: HttpErrorResponse) => this.notify.error(extractErrorMessage(err)),
    });
  }

  private reload(resetPage = false): void {
    if (resetPage) this.pageIndex = 0;
    this.loading.set(true);
    this.api.list(this.search.value.trim(), this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
