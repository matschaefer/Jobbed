import { HttpClient, HttpEvent, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApplicationDocument, DocumentType } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/documents`;
  list(applicationId: string): Observable<ApplicationDocument[]> {
    return this.http.get<ApplicationDocument[]>(this.base, { params: { applicationId } });
  }
  upload(applicationId: string, type: DocumentType, description: string, file: File): Observable<HttpEvent<ApplicationDocument>> {
    const data = new FormData(); data.append('applicationId', applicationId); data.append('documentType', type);
    if (description.trim()) data.append('description', description.trim()); data.append('file', file, file.name);
    return this.http.post<ApplicationDocument>(`${this.base}/upload`, data, { observe: 'events', reportProgress: true });
  }
  download(id: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.base}/${id}/download`, { observe: 'response', responseType: 'blob' });
  }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.base}/${id}`); }
}
