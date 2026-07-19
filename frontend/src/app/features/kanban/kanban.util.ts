import { ApplicationStatus, ApplicationSummary } from '../../core/models/application.model';
import { STATUS_OPTIONS } from '../../core/models/domain-options';

export interface KanbanColumn {
  status: ApplicationStatus;
  label: string;
  items: ApplicationSummary[];
}

/** Gruppiert Bewerbungen in Spalten entsprechend der Statusreihenfolge. */
export function groupByStatus(applications: ApplicationSummary[]): KanbanColumn[] {
  const columns: KanbanColumn[] = STATUS_OPTIONS.map((opt) => ({
    status: opt.value,
    label: opt.label,
    items: [],
  }));
  const byStatus = new Map(columns.map((c) => [c.status, c]));
  for (const app of applications) {
    byStatus.get(app.currentStatus)?.items.push(app);
  }
  return columns;
}

/** Statuswerte, deren Auswahl eine Bestätigung erfordert. */
export const CONFIRM_STATUSES: ApplicationStatus[] = ['REJECTED', 'WITHDRAWN'];

export function needsConfirmation(status: ApplicationStatus): boolean {
  return CONFIRM_STATUSES.includes(status);
}
