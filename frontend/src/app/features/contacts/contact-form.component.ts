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
import { MatButtonModule } from '@angular/material/button';
import {
  MatAutocompleteModule,
  MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ContactApi } from '../../core/api/contact.api';
import { CompanyApi } from '../../core/api/company.api';
import { CompanySummary } from '../../core/models/application.model';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { CanComponentDeactivate } from '../../core/guards/unsaved-changes.guard';

@Component({
  selector: 'app-contact-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatAutocompleteModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './contact-form.component.html',
  styleUrl: '../applications/application-form.component.scss',
})
export class ContactFormComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ContactApi);
  private readonly companyApi = inject(CompanyApi);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  readonly id = input<string>('');
  protected readonly isEdit = computed(() => this.id().length > 0);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  private saved = false;

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
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    position: [''],
    email: ['', [Validators.email]],
    phone: [''],
    linkedInUrl: [''],
    notes: [''],
  });

  ngOnInit(): void {
    if (this.isEdit()) {
      this.loading.set(true);
      this.api.get(this.id()).subscribe({
        next: (c) => {
          this.companySearch.setValue(c.company);
          this.form.patchValue({
            companyId: c.company.id,
            firstName: c.firstName,
            lastName: c.lastName,
            position: c.position ?? '',
            email: c.email ?? '',
            phone: c.phone ?? '',
            linkedInUrl: c.linkedInUrl ?? '',
            notes: c.notes ?? '',
          });
          this.form.markAsPristine();
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
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
    this.form.patchValue({ companyId: company.id });
    this.form.markAsDirty();
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const payload = this.form.getRawValue();
    const request$ = this.isEdit() ? this.api.update(this.id(), payload) : this.api.create(payload);
    request$.subscribe({
      next: () => {
        this.saved = true;
        this.notify.success('Kontakt gespeichert.');
        void this.router.navigate(['/app/contacts']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.notify.error(extractErrorMessage(err));
      },
    });
  }

  private searchCompanies(value: string | CompanySummary): Observable<CompanySummary[]> {
    if (value && typeof value !== 'string') {
      return of([]);
    }
    return this.companyApi.autocomplete(value ?? '');
  }
}
