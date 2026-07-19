import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { ApplicationApi } from '../../core/api/application.api';
import { ScheduleApi } from '../../core/api/schedule.api';
import { NotificationService } from '../../core/notifications/notification.service';
import { ApplicationSummary } from '../../core/models/application.model';
import { Interview, InterviewRequest, InterviewResult, InterviewType, Reminder } from '../../core/models/schedule.model';
import { buildMonthGrid, localDateKey, toLocalInput } from './calendar.util';

const INTERVIEW_TYPES: readonly { value: InterviewType; label: string }[] = [
  { value: 'PHONE', label: 'Telefon' }, { value: 'VIDEO', label: 'Video' },
  { value: 'ONSITE', label: 'Vor Ort' }, { value: 'TECHNICAL', label: 'Technisch' },
  { value: 'HR', label: 'HR' }, { value: 'CULTURAL_FIT', label: 'Culture Fit' },
  { value: 'FINAL', label: 'Final' }, { value: 'OTHER', label: 'Sonstiges' },
];
const RESULTS: readonly { value: InterviewResult; label: string }[] = [
  { value: 'PENDING', label: 'Offen' }, { value: 'PASSED', label: 'Bestanden' },
  { value: 'FAILED', label: 'Nicht bestanden' }, { value: 'CANCELLED', label: 'Abgesagt' },
  { value: 'NO_SHOW', label: 'Nicht erschienen' },
];

@Component({
  selector: 'app-calendar', standalone: true,
  imports: [DatePipe, ReactiveFormsModule, MatCheckboxModule, MatProgressBarModule, MatSelectModule],
  templateUrl: './calendar.component.html', styleUrl: './calendar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CalendarComponent implements OnInit {
  private readonly api = inject(ScheduleApi);
  private readonly applicationApi = inject(ApplicationApi);
  private readonly fb = inject(FormBuilder);
  private readonly notify = inject(NotificationService);

  protected readonly interviewTypes = INTERVIEW_TYPES;
  protected readonly results = RESULTS;
  protected readonly anchor = signal(new Date());
  protected readonly days = computed(() => buildMonthGrid(this.anchor()));
  protected readonly interviews = signal<Interview[]>([]);
  protected readonly reminders = signal<Reminder[]>([]);
  protected readonly applications = signal<ApplicationSummary[]>([]);
  protected readonly loading = signal(false);
  protected readonly loadError = signal(false);
  protected readonly editorOpen = signal(false);
  protected readonly reminderOpen = signal(false);
  protected readonly editing = signal<Interview | null>(null);
  protected readonly monthTitle = computed(() => new Intl.DateTimeFormat('de-DE', { month: 'long', year: 'numeric' }).format(this.anchor()));
  protected readonly upcoming = computed(() => this.interviews()
    .filter(i => new Date(i.endDateTime) >= new Date())
    .sort((a, b) => a.startDateTime.localeCompare(b.startDateTime)).slice(0, 6));

  protected readonly form = this.fb.nonNullable.group({
    applicationId: ['', Validators.required], interviewType: ['VIDEO' as InterviewType, Validators.required],
    title: ['', [Validators.required, Validators.maxLength(200)]], startLocal: ['', Validators.required],
    endLocal: ['', Validators.required], timeZone: [Intl.DateTimeFormat().resolvedOptions().timeZone || 'Europe/Berlin', Validators.required],
    location: [''], meetingUrl: [''], interviewerNames: [''], notes: [''],
    result: ['PENDING' as InterviewResult], reminderEnabled: [true], reminderMinutesBefore: [60, [Validators.required, Validators.min(0)]],
  });
  protected readonly reminderForm = this.fb.nonNullable.group({
    applicationId: [''], title: ['', Validators.required], description: [''], reminderLocal: ['', Validators.required],
  });

  ngOnInit(): void { this.load(); }
  protected previousMonth(): void { const a = this.anchor(); this.anchor.set(new Date(a.getFullYear(), a.getMonth()-1, 1)); this.load(); }
  protected nextMonth(): void { const a = this.anchor(); this.anchor.set(new Date(a.getFullYear(), a.getMonth()+1, 1)); this.load(); }
  protected today(): void { this.anchor.set(new Date()); this.load(); }
  protected interviewsFor(key: string): Interview[] { return this.interviews().filter(i => localDateKey(new Date(i.startDateTime)) === key); }
  protected remindersFor(key: string): Reminder[] { return this.reminders().filter(i => localDateKey(new Date(i.reminderDateTime)) === key); }
  protected typeLabel(type: InterviewType): string { return INTERVIEW_TYPES.find(v => v.value === type)?.label ?? type; }

  protected newInterview(day?: Date): void {
    this.editing.set(null); const start = day ? new Date(day) : new Date(); start.setHours(day ? 10 : start.getHours()+1, 0, 0, 0);
    const end = new Date(start.getTime()+60*60*1000);
    this.form.reset({ applicationId: '', interviewType: 'VIDEO', title: '', startLocal: toLocalInput(start.toISOString()),
      endLocal: toLocalInput(end.toISOString()), timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'Europe/Berlin',
      location: '', meetingUrl: '', interviewerNames: '', notes: '', result: 'PENDING', reminderEnabled: true, reminderMinutesBefore: 60 });
    this.editorOpen.set(true);
  }
  protected edit(interview: Interview): void {
    this.editing.set(interview); this.form.reset({ applicationId: interview.applicationId, interviewType: interview.interviewType,
      title: interview.title, startLocal: toLocalInput(interview.startDateTime), endLocal: toLocalInput(interview.endDateTime),
      timeZone: interview.timeZone, location: interview.location ?? '', meetingUrl: interview.meetingUrl ?? '',
      interviewerNames: interview.interviewerNames ?? '', notes: interview.notes ?? '', result: interview.result,
      reminderEnabled: interview.reminderEnabled, reminderMinutesBefore: interview.reminderMinutesBefore }); this.editorOpen.set(true);
  }
  protected save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    if (new Date(v.endLocal) <= new Date(v.startLocal)) { this.notify.error('Das Ende muss nach dem Beginn liegen.'); return; }
    const body: InterviewRequest = { applicationId: v.applicationId, interviewType: v.interviewType, title: v.title.trim(),
      startDateTime: new Date(v.startLocal).toISOString(), endDateTime: new Date(v.endLocal).toISOString(), timeZone: v.timeZone,
      location: v.location || undefined, meetingUrl: v.meetingUrl || undefined, interviewerNames: v.interviewerNames || undefined,
      notes: v.notes || undefined, result: v.result, reminderEnabled: v.reminderEnabled, reminderMinutesBefore: v.reminderMinutesBefore };
    const current = this.editing(); const request = current ? this.api.updateInterview(current.id, body) : this.api.createInterview(body);
    request.subscribe({ next: () => { this.notify.success(current ? 'Interview aktualisiert.' : 'Interview geplant.'); this.editorOpen.set(false); this.load(); } });
  }
  protected remove(): void {
    const current = this.editing(); if (!current || !confirm('Interview wirklich löschen?')) return;
    this.api.deleteInterview(current.id).subscribe(() => { this.editorOpen.set(false); this.notify.success('Interview gelöscht.'); this.load(); });
  }
  protected newReminder(day?: Date): void { const when = day ? new Date(day) : new Date(Date.now()+24*60*60*1000); when.setHours(day ? 9 : when.getHours(), 0, 0, 0);
    this.reminderForm.reset({ applicationId: '', title: '', description: '', reminderLocal: toLocalInput(when.toISOString()) }); this.reminderOpen.set(true); }
  protected saveReminder(): void { if (this.reminderForm.invalid) { this.reminderForm.markAllAsTouched(); return; }
    const v = this.reminderForm.getRawValue(); this.api.createReminder({ applicationId: v.applicationId || undefined, title: v.title.trim(),
      description: v.description || undefined, reminderDateTime: new Date(v.reminderLocal).toISOString() }).subscribe(() => {
        this.reminderOpen.set(false); this.notify.success('Erinnerung angelegt.'); this.load(); }); }
  protected complete(reminder: Reminder): void { this.api.completeReminder(reminder.id).subscribe(() => { this.notify.success('Erinnerung erledigt.'); this.load(); }); }

  private load(): void {
    this.loading.set(true); this.loadError.set(false); const grid = buildMonthGrid(this.anchor());
    const from = new Date(grid[0].date); from.setHours(0,0,0,0); const to = new Date(grid[41].date); to.setHours(23,59,59,999);
    forkJoin({ interviews: this.api.interviews(from.toISOString(), to.toISOString()), reminders: this.api.reminders(false),
      applications: this.applicationApi.list({ page: 0, size: 200, sort: 'jobTitle,asc' }) }).subscribe({
      next: ({ interviews, reminders, applications }) => { this.interviews.set(interviews); this.reminders.set(reminders); this.applications.set(applications.content); this.loading.set(false); },
      error: () => { this.loading.set(false); this.loadError.set(true); this.notify.error('Kalenderdaten konnten nicht geladen werden.'); },
    });
  }

  protected retry(): void { this.load(); }
}
