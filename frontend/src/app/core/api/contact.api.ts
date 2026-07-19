import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Contact, ContactRequest } from '../models/application.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ContactApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/contacts`;

  list(companyId: string | null, page: number, size: number): Observable<Page<Contact>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (companyId) params = params.set('companyId', companyId);
    return this.http.get<Page<Contact>>(this.base, { params });
  }

  byCompany(companyId: string): Observable<Contact[]> {
    return this.http.get<Contact[]>(`${this.base}/by-company/${companyId}`);
  }

  get(id: string): Observable<Contact> {
    return this.http.get<Contact>(`${this.base}/${id}`);
  }

  create(payload: ContactRequest): Observable<Contact> {
    return this.http.post<Contact>(this.base, payload);
  }

  update(id: string, payload: ContactRequest): Observable<Contact> {
    return this.http.put<Contact>(`${this.base}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
