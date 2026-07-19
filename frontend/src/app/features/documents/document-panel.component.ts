import { ChangeDetectionStrategy, Component, OnInit, inject, input, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse, HttpEventType } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { DocumentApi } from '../../core/api/document.api';
import { ApplicationDocument, DocumentType } from '../../core/models/document.model';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';

const TYPES: readonly { value: DocumentType; label: string }[] = [
  { value: 'CV', label: 'Lebenslauf' }, { value: 'COVER_LETTER', label: 'Anschreiben' },
  { value: 'CERTIFICATE', label: 'Zeugnis' }, { value: 'REFERENCE', label: 'Referenz' },
  { value: 'PORTFOLIO', label: 'Portfolio' }, { value: 'JOB_DESCRIPTION', label: 'Stellenanzeige' },
  { value: 'OTHER', label: 'Sonstiges' },
];

@Component({
  selector: 'app-document-panel', standalone: true, imports: [DatePipe, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush, templateUrl: './document-panel.component.html',
})
export class DocumentPanelComponent implements OnInit {
  private readonly api = inject(DocumentApi); private readonly notify = inject(NotificationService);
  readonly applicationId = input.required<string>();
  readonly changed = output<void>();
  protected readonly types = TYPES; protected readonly documents = signal<ApplicationDocument[]>([]);
  protected readonly loading = signal(false); protected readonly uploading = signal(false); protected readonly progress = signal(0);
  protected readonly selected = signal<File | null>(null); protected readonly dragActive = signal(false);
  protected readonly type = new FormControl<DocumentType>('CV', { nonNullable: true });
  protected readonly description = new FormControl('', { nonNullable: true });

  ngOnInit(): void { this.load(); }
  protected choose(event: Event): void { const input = event.target as HTMLInputElement; this.accept(input.files?.[0] ?? null); }
  protected dragOver(event: DragEvent): void { event.preventDefault(); this.dragActive.set(true); }
  protected dragLeave(event: DragEvent): void { event.preventDefault(); this.dragActive.set(false); }
  protected drop(event: DragEvent): void { event.preventDefault(); this.dragActive.set(false); this.accept(event.dataTransfer?.files?.[0] ?? null); }
  protected clear(): void { this.selected.set(null); this.progress.set(0); }
  protected upload(): void {
    const file = this.selected(); if (!file || this.uploading()) return; this.uploading.set(true); this.progress.set(0);
    this.api.upload(this.applicationId(), this.type.value, this.description.value, file).subscribe({
      next: event => { if (event.type === HttpEventType.UploadProgress) this.progress.set(Math.round(100 * event.loaded / (event.total ?? event.loaded)));
        if (event.type === HttpEventType.Response) { this.uploading.set(false); this.clear(); this.description.reset(); this.notify.success('Dokument sicher hochgeladen.'); this.changed.emit(); this.load(); } },
      error: (err: HttpErrorResponse) => { this.uploading.set(false); this.notify.error(extractErrorMessage(err)); },
    });
  }
  protected download(item: ApplicationDocument): void { this.api.download(item.id).subscribe({ next: response => {
    const blob = response.body; if (!blob) return; const url = URL.createObjectURL(blob); const link = document.createElement('a');
    link.href = url; link.download = item.originalFileName; link.style.display = 'none'; document.body.appendChild(link);
    link.click(); link.remove(); setTimeout(() => URL.revokeObjectURL(url), 1000);
  }, error: (err: HttpErrorResponse) => this.notify.error(extractErrorMessage(err)) }); }
  protected remove(item: ApplicationDocument): void { if (!confirm(`Dokument "${item.originalFileName}" löschen?`)) return;
    this.api.delete(item.id).subscribe({ next: () => { this.notify.success('Dokument gelöscht.'); this.changed.emit(); this.load(); }, error: (err: HttpErrorResponse) => this.notify.error(extractErrorMessage(err)) }); }
  protected typeLabel(value: DocumentType): string { return TYPES.find(t => t.value === value)?.label ?? value; }
  protected size(bytes: number): string { return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`; }
  private accept(file: File | null): void { if (!file) return; if (file.size > 10 * 1024 * 1024) { this.notify.error('Die Datei darf maximal 10 MB groß sein.'); return; } this.selected.set(file); }
  private load(): void { this.loading.set(true); this.api.list(this.applicationId()).subscribe({ next: values => { this.documents.set(values); this.loading.set(false); }, error: () => this.loading.set(false) }); }
}
