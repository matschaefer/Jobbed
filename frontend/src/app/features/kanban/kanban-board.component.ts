import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import {
  CdkDragDrop,
  DragDropModule,
  moveItemInArray,
  transferArrayItem,
} from '@angular/cdk/drag-drop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { ApplicationApi } from '../../core/api/application.api';
import { TagApi } from '../../core/api/tag.api';
import {
  ApplicationStatus,
  ApplicationSummary,
  Priority,
  Tag,
} from '../../core/models/application.model';
import { PRIORITY_OPTIONS, STATUS_OPTIONS, priorityLabel } from '../../core/models/domain-options';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { CompanyAvatarComponent } from '../../shared/company-avatar.component';
import { KanbanColumn, groupByStatus, needsConfirmation } from './kanban.util';

/**
 * Kanban-Board mit CDK Drag & Drop. Statusänderungen werden optimistisch
 * angezeigt und bei API-Fehlern zurückgerollt. Für Tastatur/Screenreader gibt es
 * je Karte ein Status-Menü als zugängliche Alternative zum Ziehen.
 *
 * Bewusst Standard-Change-Detection: CDK mutiert die Spalten-Arrays direkt.
 */
@Component({
  selector: 'app-kanban-board',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    DragDropModule,
    MatFormFieldModule,
    MatSelectModule,
    MatMenuModule,
    MatProgressBarModule,
    CompanyAvatarComponent,
  ],
  templateUrl: './kanban-board.component.html',
  styleUrl: './kanban-board.component.scss',
})
export class KanbanBoardComponent implements OnInit {
  private readonly api = inject(ApplicationApi);
  private readonly tagApi = inject(TagApi);
  private readonly notify = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly priorityOptions = PRIORITY_OPTIONS;
  protected readonly priorityLabel = priorityLabel;
  protected readonly stageGroups: readonly {
    title: string;
    subtitle: string;
    statuses: readonly ApplicationStatus[];
  }[] = [
    { title: 'Vorbereitung', subtitle: 'Entdecken und vorbereiten', statuses: ['SAVED', 'PREPARING', 'APPLIED'] },
    { title: 'Auswahlprozess', subtitle: 'Vom Screening bis zum finalen Gespräch', statuses: ['SCREENING', 'INTERVIEW', 'TECHNICAL_INTERVIEW', 'FINAL_INTERVIEW'] },
    { title: 'Entscheidung', subtitle: 'Angebote und Zusagen', statuses: ['OFFER', 'ACCEPTED'] },
    { title: 'Abgeschlossen', subtitle: 'Beendete und archivierte Bewerbungen', statuses: ['REJECTED', 'WITHDRAWN', 'ARCHIVED'] },
  ];

  protected readonly columns = signal<KanbanColumn[]>(groupByStatus([]));
  protected readonly loading = signal(false);
  protected allTags: Tag[] = [];

  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly priorityFilter = new FormControl<Priority | null>(null);
  protected readonly tagFilter = new FormControl<string[]>([], { nonNullable: true });

  ngOnInit(): void {
    this.tagApi.list().subscribe((tags) => (this.allTags = tags));
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
    this.priorityFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
    this.tagFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
    this.load();
  }

  trackColumn = (_: number, column: KanbanColumn): string => column.status;
  trackCard = (_: number, card: ApplicationSummary): string => card.id;

  columnsFor(statuses: readonly ApplicationStatus[]): KanbanColumn[] {
    return this.columns().filter((column) => statuses.includes(column.status));
  }

  /** Reaktion auf einen Drag&Drop-Vorgang. */
  drop(event: CdkDragDrop<ApplicationSummary[]>, target: KanbanColumn): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }
    const card = event.previousContainer.data[event.previousIndex];
    const previousStatus = card.currentStatus;
    if (needsConfirmation(target.status) && !this.confirmStatus(target.status)) {
      return;
    }
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    card.currentStatus = target.status;
    this.persist(card, previousStatus, target.status);
  }

  /** Tastaturzugängliche Statusänderung über das Karten-Menü. */
  moveViaMenu(card: ApplicationSummary, target: ApplicationStatus): void {
    if (card.currentStatus === target) {
      return;
    }
    if (needsConfirmation(target) && !this.confirmStatus(target)) {
      return;
    }
    const source = this.columns().find((c) => c.status === card.currentStatus);
    const dest = this.columns().find((c) => c.status === target);
    if (!source || !dest) {
      return;
    }
    const previousStatus = card.currentStatus;
    source.items = source.items.filter((c) => c.id !== card.id);
    card.currentStatus = target;
    dest.items = [card, ...dest.items];
    this.persist(card, previousStatus, target);
  }

  private persist(
    card: ApplicationSummary,
    previousStatus: ApplicationStatus,
    target: ApplicationStatus,
  ): void {
    this.api.changeStatus(card.id, target).subscribe({
      next: () => this.notify.success('Status aktualisiert.'),
      error: (err: HttpErrorResponse) => {
        card.currentStatus = previousStatus;
        this.notify.error(extractErrorMessage(err));
        this.load(); // konsistenten Zustand wiederherstellen
      },
    });
  }

  private confirmStatus(status: ApplicationStatus): boolean {
    const label = STATUS_OPTIONS.find((o) => o.value === status)?.label ?? status;
    return confirm(`Bewerbung wirklich auf "${label}" setzen?`);
  }

  private load(): void {
    this.loading.set(true);
    this.api
      .list({
        page: 0,
        size: 200,
        query: this.search.value.trim() || undefined,
        priority: this.priorityFilter.value ?? undefined,
        tagId: this.tagFilter.value.length ? this.tagFilter.value : undefined,
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => {
          this.columns.set(groupByStatus(page?.content ?? []));
        },
        error: () => {
          this.columns.set(groupByStatus([]));
        },
      });
  }
}
