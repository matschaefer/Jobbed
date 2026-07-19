import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  MatAutocompleteModule,
  MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ApplicationApi } from '../../core/api/application.api';
import { CompanyApi } from '../../core/api/company.api';
import { ContactApi } from '../../core/api/contact.api';
import { TagApi } from '../../core/api/tag.api';
import {
  ApplicationStatus,
  CompanySummary,
  Contact,
  EmploymentType,
  Priority,
  Tag,
  WorkModel,
} from '../../core/models/application.model';
import {
  EMPLOYMENT_TYPE_OPTIONS,
  PRIORITY_OPTIONS,
  STATUS_OPTIONS,
  WORK_MODEL_OPTIONS,
} from '../../core/models/domain-options';
import { fromIsoDate, toIsoDate } from '../../core/models/date.util';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { CanComponentDeactivate } from '../../core/guards/unsaved-changes.guard';

@Component({
  selector: 'app-application-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './application-form.component.html',
  styleUrl: './application-form.component.scss',
})
export class ApplicationFormComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly applicationApi = inject(ApplicationApi);
  private readonly companyApi = inject(CompanyApi);
  private readonly contactApi = inject(ContactApi);
  private readonly tagApi = inject(TagApi);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  /** Bewerbungs-ID aus der Route (leer = Neuanlage). */
  readonly id = input<string>('');

  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly priorityOptions = PRIORITY_OPTIONS;
  protected readonly workModelOptions = WORK_MODEL_OPTIONS;
  protected readonly employmentTypeOptions = EMPLOYMENT_TYPE_OPTIONS;

  protected readonly isEdit = computed(() => this.id().length > 0);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  private saved = false;

  protected readonly contacts = signal<Contact[]>([]);
  protected readonly allTags = signal<Tag[]>([]);
  protected readonly newTagName = new FormControl('', { nonNullable: true });

  protected readonly companySearch = new FormControl<string | CompanySummary>('', {
    nonNullable: true,
  });
  protected readonly companyOptions = toSignal(
    this.companySearch.valueChanges.pipe(
      debounceTime(250),
      switchMap((value) => this.searchCompanies(value)),
    ),
    { initialValue: [] as CompanySummary[] },
  );

  protected readonly form = this.fb.nonNullable.group({
    companyId: ['', Validators.required],
    contactPersonId: this.fb.control<string | null>(null),
    jobTitle: ['', [Validators.required, Validators.maxLength(200)]],
    jobDescription: [''],
    source: [''],
    jobUrl: [''],
    location: [''],
    employmentType: this.fb.control<string | null>(null),
    workModel: this.fb.control<string | null>(null),
    salaryMin: this.fb.control<number | null>(null),
    salaryMax: this.fb.control<number | null>(null),
    currency: ['EUR'],
    applicationDate: this.fb.control<Date | null>(null),
    deadline: this.fb.control<Date | null>(null),
    nextActionDate: this.fb.control<Date | null>(null),
    currentStatus: ['SAVED'],
    priority: ['MEDIUM'],
    rating: this.fb.control<number | null>(null),
    notes: [''],
    tagIds: this.fb.control<string[]>([]),
  });

  ngOnInit(): void {
    this.tagApi.list().subscribe((tags) => this.allTags.set(tags));
    if (this.isEdit()) {
      this.loadForEdit(this.id());
    }
  }

  canDeactivate(): boolean {
    return this.saved || this.form.pristine;
  }

  displayCompany(company: CompanySummary | string | null): string {
    return company && typeof company !== 'string' ? company.name : (company ?? '');
  }

  onCompanySelected(event: MatAutocompleteSelectedEvent): void {
    const company = event.option.value as CompanySummary;
    this.form.patchValue({ companyId: company.id, contactPersonId: null });
    this.form.markAsDirty();
    this.loadContacts(company.id);
  }

  addTag(): void {
    const name = this.newTagName.value.trim();
    if (!name) return;
    this.tagApi.create({ name }).subscribe({
      next: (tag) => {
        this.allTags.update((tags) => [...tags, tag]);
        this.form.controls.tagIds.setValue([...(this.form.controls.tagIds.value ?? []), tag.id]);
        this.form.markAsDirty();
        this.newTagName.reset();
      },
      error: (err: HttpErrorResponse) => this.notify.error(extractErrorMessage(err)),
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const v = this.form.getRawValue();
    const payload = {
      companyId: v.companyId,
      contactPersonId: v.contactPersonId,
      jobTitle: v.jobTitle,
      jobDescription: v.jobDescription || null,
      source: v.source || null,
      jobUrl: v.jobUrl || null,
      location: v.location || null,
      employmentType: (v.employmentType as EmploymentType | null) || null,
      workModel: (v.workModel as WorkModel | null) || null,
      salaryMin: v.salaryMin,
      salaryMax: v.salaryMax,
      currency: v.currency || null,
      applicationDate: toIsoDate(v.applicationDate),
      deadline: toIsoDate(v.deadline),
      nextActionDate: toIsoDate(v.nextActionDate),
      currentStatus: v.currentStatus as ApplicationStatus,
      priority: v.priority as Priority,
      rating: v.rating,
      notes: v.notes || null,
      tagIds: v.tagIds ?? [],
    };
    const request$ = this.isEdit()
      ? this.applicationApi.update(this.id(), payload)
      : this.applicationApi.create(payload);
    request$.subscribe({
      next: (app) => {
        this.saved = true;
        this.notify.success('Bewerbung gespeichert.');
        void this.router.navigate(['/app/applications', app.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.notify.error(extractErrorMessage(err));
      },
    });
  }

  private loadForEdit(id: string): void {
    this.loading.set(true);
    this.applicationApi.get(id).subscribe({
      next: (app) => {
        this.companySearch.setValue(app.company);
        this.loadContacts(app.company.id);
        this.form.patchValue({
          companyId: app.company.id,
          contactPersonId: app.contactPerson?.id ?? null,
          jobTitle: app.jobTitle,
          jobDescription: app.jobDescription ?? '',
          source: app.source ?? '',
          jobUrl: app.jobUrl ?? '',
          location: app.location ?? '',
          employmentType: app.employmentType ?? null,
          workModel: app.workModel ?? null,
          salaryMin: app.salaryMin ?? null,
          salaryMax: app.salaryMax ?? null,
          currency: app.currency ?? 'EUR',
          applicationDate: fromIsoDate(app.applicationDate),
          deadline: fromIsoDate(app.deadline),
          nextActionDate: fromIsoDate(app.nextActionDate),
          currentStatus: app.currentStatus,
          priority: app.priority ?? 'MEDIUM',
          rating: app.rating ?? null,
          notes: app.notes ?? '',
          tagIds: app.tags.map((t) => t.id),
        });
        this.form.markAsPristine();
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadContacts(companyId: string): void {
    this.contactApi.byCompany(companyId).subscribe((contacts) => this.contacts.set(contacts));
  }

  private searchCompanies(value: string | CompanySummary): Observable<CompanySummary[]> {
    if (value && typeof value !== 'string') {
      return of([]);
    }
    return this.companyApi.autocomplete(value ?? '');
  }
}
