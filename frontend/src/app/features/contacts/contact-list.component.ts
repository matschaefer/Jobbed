import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ContactApi } from '../../core/api/contact.api';
import { Contact } from '../../core/models/application.model';
import { Page } from '../../core/models/page.model';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { CompanyAvatarComponent } from '../../shared/company-avatar.component';

@Component({
  selector: 'app-contact-list',
  standalone: true,
  imports: [
    RouterLink,
    MatPaginatorModule,
    MatProgressBarModule,
    CompanyAvatarComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './contact-list.component.html',
  styleUrl: '../applications/application-list.component.scss',
})
export class ContactListComponent implements OnInit {
  private readonly api = inject(ContactApi);
  private readonly notify = inject(NotificationService);

  protected readonly loading = signal(false);
  protected readonly page = signal<Page<Contact> | null>(null);
  private pageIndex = 0;
  private pageSize = 20;

  ngOnInit(): void {
    this.reload();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.reload();
  }

  remove(contact: Contact): void {
    if (!confirm(`Kontakt "${contact.firstName} ${contact.lastName}" löschen?`)) {
      return;
    }
    this.api.delete(contact.id).subscribe({
      next: () => {
        this.notify.success('Kontakt gelöscht.');
        this.reload();
      },
      error: (err: HttpErrorResponse) => this.notify.error(extractErrorMessage(err)),
    });
  }

  private reload(): void {
    this.loading.set(true);
    this.api.list(null, this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
