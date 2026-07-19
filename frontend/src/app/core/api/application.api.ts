import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Activity,
  ApplicationDetail,
  ApplicationListParams,
  ApplicationRequest,
  ApplicationStatus,
  ApplicationSummary,
} from '../models/application.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ApplicationApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/applications`;

  list(params: ApplicationListParams): Observable<Page<ApplicationSummary>> {
    let httpParams = new HttpParams();
    if (params.page != null) httpParams = httpParams.set('page', params.page);
    if (params.size != null) httpParams = httpParams.set('size', params.size);
    if (params.sort) httpParams = httpParams.set('sort', params.sort);
    if (params.query) httpParams = httpParams.set('query', params.query);
    if (params.companyId) httpParams = httpParams.set('companyId', params.companyId);
    if (params.priority) httpParams = httpParams.set('priority', params.priority);
    if (params.workModel) httpParams = httpParams.set('workModel', params.workModel);
    for (const status of params.status ?? []) httpParams = httpParams.append('status', status);
    for (const tagId of params.tagId ?? []) httpParams = httpParams.append('tagId', tagId);
    return this.http.get<Page<ApplicationSummary>>(this.base, { params: httpParams });
  }

  get(id: string): Observable<ApplicationDetail> {
    return this.http.get<ApplicationDetail>(`${this.base}/${id}`);
  }

  create(payload: ApplicationRequest): Observable<ApplicationDetail> {
    return this.http.post<ApplicationDetail>(this.base, payload);
  }

  update(id: string, payload: ApplicationRequest): Observable<ApplicationDetail> {
    return this.http.put<ApplicationDetail>(`${this.base}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  changeStatus(
    id: string,
    newStatus: ApplicationStatus,
    note?: string,
  ): Observable<ApplicationDetail> {
    return this.http.patch<ApplicationDetail>(`${this.base}/${id}/status`, { newStatus, note });
  }

  activities(id: string): Observable<Page<Activity>> {
    return this.http.get<Page<Activity>>(`${this.base}/${id}/activities`);
  }

  addActivity(
    id: string,
    body: { activityType?: string; title: string; description?: string },
  ): Observable<Activity> {
    return this.http.post<Activity>(`${this.base}/${id}/activities`, body);
  }
}
