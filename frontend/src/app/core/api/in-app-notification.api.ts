import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationList } from '../models/schedule.model';

@Injectable({ providedIn: 'root' })
export class InAppNotificationApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/notifications`;
  list(): Observable<NotificationList> { return this.http.get<NotificationList>(this.base); }
  read(id: string): Observable<void> { return this.http.patch<void>(`${this.base}/${id}/read`, {}); }
  readAll(): Observable<void> { return this.http.patch<void>(`${this.base}/read-all`, {}); }
}
