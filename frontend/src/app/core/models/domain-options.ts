import { ApplicationStatus, EmploymentType, Priority, WorkModel } from './application.model';

export interface Option<T> {
  value: T;
  label: string;
}

/** Deutsche Beschriftungen + Kanban-Reihenfolge der Statuswerte. */
export const STATUS_OPTIONS: Option<ApplicationStatus>[] = [
  { value: 'SAVED', label: 'Gespeichert' },
  { value: 'PREPARING', label: 'In Vorbereitung' },
  { value: 'APPLIED', label: 'Beworben' },
  { value: 'SCREENING', label: 'Screening' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'TECHNICAL_INTERVIEW', label: 'Technisches Interview' },
  { value: 'FINAL_INTERVIEW', label: 'Finales Interview' },
  { value: 'OFFER', label: 'Angebot' },
  { value: 'ACCEPTED', label: 'Angenommen' },
  { value: 'REJECTED', label: 'Abgelehnt' },
  { value: 'WITHDRAWN', label: 'Zurückgezogen' },
  { value: 'ARCHIVED', label: 'Archiviert' },
];

export const PRIORITY_OPTIONS: Option<Priority>[] = [
  { value: 'LOW', label: 'Niedrig' },
  { value: 'MEDIUM', label: 'Mittel' },
  { value: 'HIGH', label: 'Hoch' },
  { value: 'URGENT', label: 'Dringend' },
];

export const WORK_MODEL_OPTIONS: Option<WorkModel>[] = [
  { value: 'ONSITE', label: 'Vor Ort' },
  { value: 'HYBRID', label: 'Hybrid' },
  { value: 'REMOTE', label: 'Remote' },
];

export const EMPLOYMENT_TYPE_OPTIONS: Option<EmploymentType>[] = [
  { value: 'FULL_TIME', label: 'Vollzeit' },
  { value: 'PART_TIME', label: 'Teilzeit' },
  { value: 'CONTRACT', label: 'Vertrag' },
  { value: 'INTERNSHIP', label: 'Praktikum' },
  { value: 'WORKING_STUDENT', label: 'Werkstudent' },
  { value: 'FREELANCE', label: 'Freiberuflich' },
];

const STATUS_LABELS = new Map(STATUS_OPTIONS.map((o) => [o.value, o.label]));
const PRIORITY_LABELS = new Map(PRIORITY_OPTIONS.map((o) => [o.value, o.label]));
const WORK_MODEL_LABELS = new Map(WORK_MODEL_OPTIONS.map((o) => [o.value, o.label]));

export function statusLabel(status: ApplicationStatus): string {
  return STATUS_LABELS.get(status) ?? status;
}
export function priorityLabel(priority?: Priority): string {
  return priority ? (PRIORITY_LABELS.get(priority) ?? priority) : '';
}
export function workModelLabel(workModel?: WorkModel): string {
  return workModel ? (WORK_MODEL_LABELS.get(workModel) ?? workModel) : '';
}

/** CSS-Klassensuffix je Status (nicht rein farbbasiert – zusätzlich Text-Label). */
export function statusColorClass(status: ApplicationStatus): string {
  switch (status) {
    case 'OFFER':
    case 'ACCEPTED':
      return 'status--success';
    case 'REJECTED':
    case 'WITHDRAWN':
      return 'status--danger';
    case 'INTERVIEW':
    case 'TECHNICAL_INTERVIEW':
    case 'FINAL_INTERVIEW':
      return 'status--interview';
    case 'APPLIED':
    case 'SCREENING':
      return 'status--active';
    default:
      return 'status--neutral';
  }
}
