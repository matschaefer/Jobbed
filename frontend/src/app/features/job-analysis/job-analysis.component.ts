import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { JobAnalysisApi } from '../../core/api/job-analysis.api';
import { AiStatus, JobAnalysisResult } from '../../core/models/job-analysis.model';
import { NotificationService } from '../../core/notifications/notification.service';

const SAMPLE = `Senior Java Developer (m/w/d)
Du entwickelst moderne Plattformen mit Java, Spring Boot, PostgreSQL und Docker. Erfahrung mit Kubernetes, Git und CI/CD ist von Vorteil. Wir arbeiten hybrid und kommunizieren auf Deutsch und Englisch. Dich erwarten flexible Arbeitszeiten, ein Weiterbildungsbudget und 70.000 - 90.000 EUR jährlich.`;

@Component({
  selector: 'app-job-analysis',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './job-analysis.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobAnalysisComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(JobAnalysisApi);
  private readonly notify = inject(NotificationService);

  protected readonly loading = signal(false);
  protected readonly result = signal<JobAnalysisResult | null>(null);
  protected readonly aiStatus = signal<AiStatus>({ available: false, provider: 'DISABLED', model: '' });
  protected readonly form = this.fb.nonNullable.group({
    profileSkills: ['Java, TypeScript, Angular, Docker, Git'],
    jobDescription: ['', [Validators.required, Validators.minLength(30), Validators.maxLength(50000)]],
  });
  protected readonly matchTone = computed(() => {
    const match = this.result()?.matchPercentage ?? 0;
    return match >= 70 ? 'text-emerald-400' : match >= 40 ? 'text-amber-400' : 'text-rose-400';
  });

  ngOnInit(): void {
    this.api.status().subscribe({ next: (status) => this.aiStatus.set(status) });
  }

  analyze(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const profileSkills = value.profileSkills.split(/[,;\n]/).map((skill) => skill.trim()).filter(Boolean);
    this.loading.set(true);
    this.api.analyze({ jobDescription: value.jobDescription, profileSkills })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => this.result.set(result),
        error: () => this.notify.error('Die Stellenanzeige konnte nicht analysiert werden.'),
      });
  }

  useSample(): void {
    this.form.controls.jobDescription.setValue(SAMPLE);
    this.result.set(null);
  }

  seniorityLabel(value: JobAnalysisResult['seniorityLevel']): string {
    return ({ JUNIOR: 'Junior', SENIOR: 'Senior', LEAD: 'Lead / Principal', NOT_SPECIFIED: 'Nicht angegeben' })[value];
  }

  workModelLabel(value: JobAnalysisResult['workModel']): string {
    return ({ REMOTE: 'Remote', HYBRID: 'Hybrid', ON_SITE: 'Vor Ort', NOT_SPECIFIED: 'Nicht angegeben' })[value];
  }

  analysisSource(value: string): string {
    if (value.startsWith('OPENAI:')) return `KI-Analyse · ${value.slice('OPENAI:'.length)}`;
    if (value === 'RULE_BASED_FALLBACK') return 'Regelbasierter Fallback · KI war vorübergehend nicht erreichbar';
    return 'Regelbasierte Analyse · für KI bitte API-Key konfigurieren';
  }
}
