import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResumeGenerationRequest, ResumeResult } from '../models/resume.model';
import { AiStatus } from '../models/job-analysis.model';

@Injectable({ providedIn: 'root' })
export class ResumeApi {
  private readonly http = inject(HttpClient);

  generate(request: ResumeGenerationRequest): Observable<ResumeResult> {
    return this.http.post<ResumeResult>(`${environment.apiBaseUrl}/resume/generate`, request);
  }

  status(): Observable<AiStatus> {
    return this.http.get<AiStatus>(`${environment.apiBaseUrl}/ai/status`);
  }
}
