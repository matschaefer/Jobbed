import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Tag, TagRequest } from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class TagApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/tags`;

  list(): Observable<Tag[]> {
    return this.http.get<Tag[]>(this.base);
  }

  create(payload: TagRequest): Observable<Tag> {
    return this.http.post<Tag>(this.base, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
