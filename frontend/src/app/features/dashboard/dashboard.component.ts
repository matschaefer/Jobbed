import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe, PercentPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AnalyticsApi } from '../../core/api/analytics.api';
import { AuthStore } from '../../core/auth/auth.store';
import {
  AnalyticsOverview,
  CompanyPerformance,
  SourcePerformance,
  StatusCount,
  SuccessRates,
  TimeSeries,
} from '../../core/models/analytics.model';
import { statusLabel } from '../../core/models/domain-options';

const BRAND = '#8b7cf6';
const AXIS = '#8b909c';
const GRID = 'rgba(148,163,184,0.12)';
const DOUGHNUT_PALETTE = [
  '#8b7cf6',
  '#22c55e',
  '#f5b301',
  '#2d8cff',
  '#ef4444',
  '#ec4899',
  '#06b6d4',
  '#f97316',
  '#a855f7',
  '#14b8a6',
  '#eab308',
  '#64748b',
];

interface Kpi {
  icon: string;
  label: string;
  value: string;
  accent?: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DecimalPipe, PercentPipe, RouterLink, BaseChartDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(AnalyticsApi);
  protected readonly store = inject(AuthStore);

  protected readonly loading = signal(true);
  protected readonly overview = signal<AnalyticsOverview | null>(null);
  protected readonly rates = signal<SuccessRates | null>(null);
  protected readonly sources = signal<SourcePerformance[]>([]);
  protected readonly companies = signal<CompanyPerformance[]>([]);
  private readonly statusDist = signal<StatusCount[]>([]);
  private readonly timeSeries = signal<TimeSeries | null>(null);
  protected readonly granularity = signal<'month' | 'week'>('month');

  protected readonly kpis = computed<Kpi[]>(() => {
    const o = this.overview();
    if (!o) return [];
    return [
      { icon: 'work_outline', label: 'Bewerbungen gesamt', value: `${o.totalApplications}` },
      { icon: 'calendar_month', label: 'Diesen Monat', value: `${o.applicationsThisMonth}` },
      { icon: 'pending_actions', label: 'Offen', value: `${o.openApplications}` },
      { icon: 'event', label: 'Anstehende Interviews', value: `${o.upcomingInterviews}` },
      { icon: 'notifications_active', label: 'Offene Follow-ups', value: `${o.pendingFollowUps}` },
      { icon: 'verified', label: 'Angebote', value: `${o.offers}`, accent: true },
      { icon: 'cancel', label: 'Absagen', value: `${o.rejections}` },
      { icon: 'trending_up', label: 'Erfolgsquote', value: this.pct(o.successRate) },
    ];
  });

  protected readonly hasData = computed(() => (this.overview()?.totalApplications ?? 0) > 0);

  protected readonly statusChart = computed<ChartData<'doughnut'>>(() => {
    const dist = this.statusDist();
    return {
      labels: dist.map((d) => statusLabel(d.status)),
      datasets: [
        {
          data: dist.map((d) => d.count),
          backgroundColor: dist.map((_, i) => DOUGHNUT_PALETTE[i % DOUGHNUT_PALETTE.length]),
          borderColor: '#14161c',
          borderWidth: 2,
          hoverOffset: 6,
        },
      ],
    };
  });

  protected readonly timeChart = computed<ChartData<'bar'>>(() => {
    const ts = this.timeSeries();
    const points = ts?.points ?? [];
    return {
      labels: points.map((p) => this.formatPeriod(p.period)),
      datasets: [
        {
          data: points.map((p) => p.count),
          backgroundColor: BRAND,
          borderRadius: 6,
          maxBarThickness: 34,
        },
      ],
    };
  });

  protected readonly funnelChart = computed<ChartData<'bar'>>(() => {
    const r = this.rates();
    return {
      labels: ['Beworben', 'Interview', 'Angebot', 'Angenommen'],
      datasets: [
        {
          data: r ? [r.applied, r.interviewed, r.offered, r.accepted] : [],
          backgroundColor: ['#2d8cff', '#8b7cf6', '#f5b301', '#22c55e'],
          borderRadius: 6,
          maxBarThickness: 30,
        },
      ],
    };
  });

  protected readonly doughnutOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '62%',
    plugins: {
      legend: { position: 'right', labels: { color: AXIS, boxWidth: 12, padding: 12 } },
    },
  };

  protected readonly barOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { color: AXIS }, grid: { display: false } },
      y: { ticks: { color: AXIS, precision: 0 }, grid: { color: GRID }, beginAtZero: true },
    },
  };

  protected readonly funnelOptions: ChartConfiguration<'bar'>['options'] = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { ticks: { color: AXIS, precision: 0 }, grid: { color: GRID }, beginAtZero: true },
      y: { ticks: { color: AXIS }, grid: { display: false } },
    },
  };

  ngOnInit(): void {
    this.load();
  }

  setGranularity(g: 'month' | 'week'): void {
    if (this.granularity() === g) return;
    this.granularity.set(g);
    this.api.applicationsOverTime(g).subscribe((ts) => this.timeSeries.set(ts));
  }

  private load(): void {
    this.loading.set(true);
    forkJoin({
      overview: this.api.overview(),
      status: this.api.statusDistribution(),
      time: this.api.applicationsOverTime(this.granularity()),
      rates: this.api.successRate(),
      sources: this.api.sourcePerformance(),
      companies: this.api.companyPerformance(),
    }).subscribe({
      next: (r) => {
        this.overview.set(r.overview);
        this.statusDist.set(r.status);
        this.timeSeries.set(r.time);
        this.rates.set(r.rates);
        this.sources.set(r.sources);
        this.companies.set(r.companies);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private pct(value: number): string {
    return `${Math.round(value * 100)} %`;
  }

  private formatPeriod(period: string): string {
    // "2026-07" -> "Jul 26"; ISO-Datum (Woche) -> "dd.MM."
    if (/^\d{4}-\d{2}$/.test(period)) {
      const [y, m] = period.split('-');
      const months = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
      return `${months[Number(m) - 1]} ${y.slice(2)}`;
    }
    const d = new Date(period);
    return `${`${d.getDate()}`.padStart(2, '0')}.${`${d.getMonth() + 1}`.padStart(2, '0')}.`;
  }
}
