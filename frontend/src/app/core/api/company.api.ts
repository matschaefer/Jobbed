import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Company, CompanyRequest, CompanySummary } from '../models/application.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class CompanyApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/companies`;

  list(query: string, page: number, size: number): Observable<Page<CompanySummary>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'name,asc');
    if (query) params = params.set('query', query);
    return this.http.get<Page<CompanySummary>>(this.base, { params });
  }

  autocomplete(query: string): Observable<CompanySummary[]> {
    const params = new HttpParams().set('query', query ?? '');
    return this.http.get<CompanySummary[]>(`${this.base}/autocomplete`, { params });
  }

  get(id: string): Observable<Company> {
    return this.http.get<Company>(`${this.base}/${id}`);
  }

  create(payload: CompanyRequest): Observable<Company> {
    return this.http.post<Company>(this.base, payload);
  }

  update(id: string, payload: CompanyRequest): Observable<Company> {
    return this.http.put<Company>(`${this.base}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
