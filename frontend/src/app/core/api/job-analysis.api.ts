import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AiStatus, JobAnalysisRequest, JobAnalysisResult } from '../models/job-analysis.model';

@Injectable({ providedIn: 'root' })
export class JobAnalysisApi {
  private readonly http = inject(HttpClient);

  analyze(request: JobAnalysisRequest): Observable<JobAnalysisResult> {
    return this.http.post<JobAnalysisResult>(`${environment.apiBaseUrl}/job-analysis/analyze`, request);
  }

  status(): Observable<AiStatus> {
    return this.http.get<AiStatus>(`${environment.apiBaseUrl}/ai/status`);
  }
}
