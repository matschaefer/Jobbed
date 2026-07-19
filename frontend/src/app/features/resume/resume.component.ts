import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ResumeApi } from '../../core/api/resume.api';
import { AuthStore } from '../../core/auth/auth.store';
import { AiStatus } from '../../core/models/job-analysis.model';
import { ResumeResult } from '../../core/models/resume.model';
import { NotificationService } from '../../core/notifications/notification.service';

@Component({
  selector: 'app-resume',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './resume.component.html',
  styleUrl: './resume.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResumeComponent implements OnInit {
  private readonly api = inject(ResumeApi);
  private readonly store = inject(AuthStore);
  private readonly fb = inject(FormBuilder);
  private readonly notify = inject(NotificationService);
  private readonly user = this.store.user();

  protected readonly loading = signal(false);
  protected readonly result = signal<ResumeResult | null>(null);
  protected readonly aiStatus = signal<AiStatus>({ available: false, provider: 'DISABLED', model: '' });
  protected readonly form = this.fb.nonNullable.group({
    fullName: [`${this.user?.firstName ?? ''} ${this.user?.lastName ?? ''}`.trim(), Validators.required],
    email: [this.user?.email ?? '', [Validators.required, Validators.email]],
    phone: [''], location: [''], headline: [''], professionalSummary: [''],
    skills: ['Java, TypeScript, Angular, Docker, Git'], experience: [''], education: [''],
    languages: ['Deutsch, Englisch'], targetRole: [''], jobDescription: [''],
  });

  ngOnInit(): void {
    this.api.status().subscribe({ next: (status) => this.aiStatus.set(status) });
  }

  protected generate(): void {
    if (this.form.invalid || this.loading()) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.api.generate(this.form.getRawValue()).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (value) => { this.result.set(value); this.notify.success('Lebenslauf-Entwurf erstellt.'); },
      error: () => this.notify.error('Der Lebenslauf konnte nicht erstellt werden.'),
    });
  }

  protected downloadHtml(): void {
    const value = this.result(); if (!value) return;
    const list = (items: string[]) => `<ul>${items.map(item => `<li>${this.escape(item)}</li>`).join('')}</ul>`;
    const experience = value.experience.map(item => `<section><h3>${this.escape(item.role)}${item.company ? ` · ${this.escape(item.company)}` : ''}</h3><small>${this.escape(item.period)}</small>${list(item.bullets)}</section>`).join('');
    const html = `<!doctype html><html lang="de"><meta charset="utf-8"><title>Lebenslauf ${this.escape(value.fullName)}</title><style>body{font:15px/1.55 Arial,sans-serif;max-width:820px;margin:40px auto;color:#20232a;padding:0 24px}h1{font-size:34px;margin:0}h2{margin-top:28px;border-bottom:2px solid #7567e8;padding-bottom:5px}h3{margin-bottom:0}small{color:#666}.skills{display:flex;flex-wrap:wrap;gap:8px}.skills span{background:#eeeafe;padding:5px 10px;border-radius:20px}@media print{body{margin:0}}</style><body><header><h1>${this.escape(value.fullName)}</h1><h3>${this.escape(value.headline)}</h3><p>${this.escape(value.contactLine)}</p></header><h2>Profil</h2><p>${this.escape(value.professionalSummary)}</p><h2>Kompetenzen</h2><div class="skills">${value.coreSkills.map(item => `<span>${this.escape(item)}</span>`).join('')}</div>${value.experience.length ? `<h2>Berufserfahrung</h2>${experience}` : ''}${value.education.length ? `<h2>Ausbildung</h2>${list(value.education)}` : ''}${value.languages.length ? `<h2>Sprachen</h2>${list(value.languages)}` : ''}</body></html>`;
    const url = URL.createObjectURL(new Blob([html], { type: 'text/html;charset=utf-8' }));
    const link = document.createElement('a'); link.href = url; link.download = `Lebenslauf-${value.fullName.replace(/[^a-z0-9äöüß]+/gi, '-')}.html`;
    link.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  protected print(): void { window.print(); }
  protected sourceLabel(value: string): string {
    if (value.startsWith('OPENAI:')) return `Mit KI erstellt · ${value.slice(7)}`;
    if (value === 'TEMPLATE_FALLBACK') return 'Professionelle Vorlage · KI war nicht erreichbar';
    return 'Professionelle Vorlage · KI-Key noch nicht konfiguriert';
  }
  private escape(value: string): string {
    return value.replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char] ?? char);
  }
}
