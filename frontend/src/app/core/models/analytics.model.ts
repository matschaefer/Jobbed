import { ApplicationStatus } from './application.model';

export interface AnalyticsOverview {
  totalApplications: number;
  applicationsThisMonth: number;
  openApplications: number;
  upcomingInterviews: number;
  pendingFollowUps: number;
  offers: number;
  rejections: number;
  successRate: number;
  interviewRate: number;
}

export interface StatusCount {
  status: ApplicationStatus;
  count: number;
}

export interface TimeBucket {
  period: string;
  count: number;
}

export interface TimeSeries {
  granularity: string;
  points: TimeBucket[];
}

export interface SuccessRates {
  total: number;
  applied: number;
  interviewed: number;
  offered: number;
  accepted: number;
  rejected: number;
  responseRate: number;
  interviewRate: number;
  offerRate: number;
  successRate: number;
}

export interface SourcePerformance {
  source: string;
  total: number;
  interviews: number;
  offers: number;
  offerRate: number;
}

export interface CompanyPerformance {
  company: string;
  total: number;
  interviews: number;
  offers: number;
}
