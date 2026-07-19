import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AccountApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/account`;

  deleteAccount(password: string): Observable<void> {
    return this.http.delete<void>(this.base, { body: { password } });
  }
}
