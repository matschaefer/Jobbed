export type ApplicationStatus =
  | 'SAVED'
  | 'PREPARING'
  | 'APPLIED'
  | 'SCREENING'
  | 'INTERVIEW'
  | 'TECHNICAL_INTERVIEW'
  | 'FINAL_INTERVIEW'
  | 'OFFER'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'WITHDRAWN'
  | 'ARCHIVED';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export type EmploymentType =
  'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP' | 'WORKING_STUDENT' | 'FREELANCE';

export type WorkModel = 'ONSITE' | 'HYBRID' | 'REMOTE';

export type ActivityType =
  | 'CREATED'
  | 'STATUS_CHANGED'
  | 'NOTE_ADDED'
  | 'EMAIL_SENT'
  | 'INTERVIEW_SCHEDULED'
  | 'FOLLOW_UP'
  | 'DOCUMENT_UPLOADED'
  | 'OFFER_RECEIVED'
  | 'REJECTED'
  | 'CUSTOM';

export interface CompanySummary {
  id: string;
  name: string;
  location?: string;
  industry?: string;
  logoUrl?: string;
}

export interface Company extends CompanySummary {
  website?: string;
  companySize?: string;
  description?: string;
  applicationCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyRequest {
  name: string;
  website?: string;
  industry?: string;
  companySize?: string;
  location?: string;
  description?: string;
  logoUrl?: string;
}

export interface Contact {
  id: string;
  company: CompanySummary;
  firstName: string;
  lastName: string;
  position?: string;
  email?: string;
  phone?: string;
  linkedInUrl?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ContactRequest {
  companyId: string;
  firstName: string;
  lastName: string;
  position?: string;
  email?: string;
  phone?: string;
  linkedInUrl?: string;
  notes?: string;
}

export interface Tag {
  id: string;
  name: string;
  color?: string;
}

export interface TagRequest {
  name: string;
  color?: string;
}

export interface ApplicationSummary {
  id: string;
  jobTitle: string;
  company: CompanySummary;
  location?: string;
  workModel?: WorkModel;
  currentStatus: ApplicationStatus;
  priority?: Priority;
  rating?: number;
  applicationDate?: string;
  nextActionDate?: string;
  deadline?: string;
  tags: Tag[];
}

export interface ApplicationDetail {
  id: string;
  company: CompanySummary;
  contactPerson?: Contact;
  jobTitle: string;
  jobDescription?: string;
  source?: string;
  jobUrl?: string;
  employmentType?: EmploymentType;
  workModel?: WorkModel;
  location?: string;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  applicationDate?: string;
  currentStatus: ApplicationStatus;
  priority?: Priority;
  rating?: number;
  deadline?: string;
  nextActionDate?: string;
  notes?: string;
  rejectionReason?: string;
  tags: Tag[];
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationRequest {
  companyId: string;
  contactPersonId?: string | null;
  jobTitle: string;
  jobDescription?: string | null;
  source?: string | null;
  jobUrl?: string | null;
  employmentType?: EmploymentType | null;
  workModel?: WorkModel | null;
  location?: string | null;
  salaryMin?: number | null;
  salaryMax?: number | null;
  currency?: string | null;
  applicationDate?: string | null;
  currentStatus?: ApplicationStatus | null;
  priority?: Priority | null;
  rating?: number | null;
  deadline?: string | null;
  nextActionDate?: string | null;
  notes?: string | null;
  rejectionReason?: string | null;
  tagIds?: string[];
}

export interface Activity {
  id: string;
  activityType: ActivityType;
  title: string;
  description?: string;
  previousStatus?: ApplicationStatus;
  newStatus?: ApplicationStatus;
  activityDate: string;
}

export interface ApplicationListParams {
  page?: number;
  size?: number;
  sort?: string;
  query?: string;
  status?: ApplicationStatus[];
  companyId?: string;
  priority?: Priority;
  workModel?: WorkModel;
  tagId?: string[];
}
