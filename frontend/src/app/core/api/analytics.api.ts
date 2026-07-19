import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AnalyticsOverview,
  CompanyPerformance,
  SourcePerformance,
  StatusCount,
  SuccessRates,
  TimeSeries,
} from '../models/analytics.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/analytics`;

  overview(): Observable<AnalyticsOverview> {
    return this.http.get<AnalyticsOverview>(`${this.base}/overview`);
  }

  statusDistribution(): Observable<StatusCount[]> {
    return this.http.get<StatusCount[]>(`${this.base}/status-distribution`);
  }

  applicationsOverTime(granularity: 'month' | 'week' = 'month'): Observable<TimeSeries> {
    return this.http.get<TimeSeries>(`${this.base}/applications-over-time`, {
      params: new HttpParams().set('granularity', granularity),
    });
  }

  successRate(): Observable<SuccessRates> {
    return this.http.get<SuccessRates>(`${this.base}/success-rate`);
  }

  sourcePerformance(): Observable<SourcePerformance[]> {
    return this.http.get<SourcePerformance[]>(`${this.base}/source-performance`);
  }

  companyPerformance(): Observable<CompanyPerformance[]> {
    return this.http.get<CompanyPerformance[]>(`${this.base}/company-performance`);
  }
}
