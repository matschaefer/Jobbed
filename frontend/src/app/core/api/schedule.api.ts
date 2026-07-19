import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Interview, InterviewRequest, Reminder, ReminderRequest } from '../models/schedule.model';

@Injectable({ providedIn: 'root' })
export class ScheduleApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  interviews(from: string, to: string): Observable<Interview[]> {
    return this.http.get<Interview[]>(`${this.base}/interviews`, { params: new HttpParams().set('from', from).set('to', to) });
  }
  createInterview(body: InterviewRequest): Observable<Interview> { return this.http.post<Interview>(`${this.base}/interviews`, body); }
  updateInterview(id: string, body: InterviewRequest): Observable<Interview> { return this.http.put<Interview>(`${this.base}/interviews/${id}`, body); }
  deleteInterview(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/interviews/${id}`); }
  reminders(completed = false): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(`${this.base}/reminders`, { params: { completed } });
  }
  createReminder(body: ReminderRequest): Observable<Reminder> { return this.http.post<Reminder>(`${this.base}/reminders`, body); }
  completeReminder(id: string): Observable<Reminder> { return this.http.patch<Reminder>(`${this.base}/reminders/${id}/complete`, {}); }
  deleteReminder(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/reminders/${id}`); }
}
