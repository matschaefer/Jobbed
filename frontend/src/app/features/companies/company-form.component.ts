import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CompanyApi } from '../../core/api/company.api';
import { extractErrorMessage } from '../../core/interceptors/error.interceptor';
import { NotificationService } from '../../core/notifications/notification.service';
import { CanComponentDeactivate } from '../../core/guards/unsaved-changes.guard';

@Component({
  selector: 'app-company-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './company-form.component.html',
  styleUrl: '../applications/application-form.component.scss',
})
export class CompanyFormComponent implements OnInit, CanComponentDeactivate {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CompanyApi);
  private readonly router = inject(Router);
  private readonly notify = inject(NotificationService);

  readonly id = input<string>('');
  protected readonly isEdit = computed(() => this.id().length > 0);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  private saved = false;

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    website: [''],
    industry: [''],
    companySize: [''],
    location: [''],
    description: [''],
    logoUrl: [''],
  });

  ngOnInit(): void {
    if (this.isEdit()) {
      this.loading.set(true);
      this.api.get(this.id()).subscribe({
        next: (c) => {
          this.form.patchValue({
            name: c.name,
            website: c.website ?? '',
            industry: c.industry ?? '',
            companySize: c.companySize ?? '',
            location: c.location ?? '',
            description: c.description ?? '',
            logoUrl: c.logoUrl ?? '',
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

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const payload = this.form.getRawValue();
    const request$ = this.isEdit() ? this.api.update(this.id(), payload) : this.api.create(payload);
    request$.subscribe({
      next: (company) => {
        this.saved = true;
        this.notify.success('Unternehmen gespeichert.');
        void this.router.navigate(['/app/companies']);
        void company;
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.notify.error(extractErrorMessage(err));
      },
    });
  }
}
