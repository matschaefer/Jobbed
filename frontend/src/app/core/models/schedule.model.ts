export type InterviewType = 'PHONE' | 'VIDEO' | 'ONSITE' | 'TECHNICAL' | 'HR' | 'CULTURAL_FIT' | 'FINAL' | 'OTHER';
export type InterviewResult = 'PENDING' | 'PASSED' | 'FAILED' | 'CANCELLED' | 'NO_SHOW';
export type ReminderType = 'CUSTOM' | 'INTERVIEW' | 'DEADLINE';
export type InAppNotificationType = 'REMINDER' | 'INTERVIEW' | 'DEADLINE' | 'INFO';

export interface Interview {
  id: string; applicationId: string; applicationTitle: string; companyName: string;
  interviewType: InterviewType; title: string; startDateTime: string; endDateTime: string;
  timeZone: string; location?: string; meetingUrl?: string; interviewerNames?: string;
  notes?: string; result: InterviewResult; reminderEnabled: boolean; reminderMinutesBefore: number;
  createdAt: string; updatedAt: string;
}

export interface InterviewRequest {
  applicationId: string; interviewType: InterviewType; title: string;
  startDateTime: string; endDateTime: string; timeZone: string; location?: string;
  meetingUrl?: string; interviewerNames?: string; notes?: string; result: InterviewResult;
  reminderEnabled: boolean; reminderMinutesBefore: number;
}

export interface Reminder {
  id: string; applicationId?: string; interviewId?: string; reminderType: ReminderType;
  title: string; description?: string; reminderDateTime: string; completed: boolean;
  sent: boolean; sentAt?: string;
}

export interface ReminderRequest {
  applicationId?: string; title: string; description?: string; reminderDateTime: string;
}

export interface InAppNotification {
  id: string; notificationType: InAppNotificationType; title: string; message: string;
  actionUrl?: string; read: boolean; createdAt: string;
}

export interface NotificationList { items: InAppNotification[]; unreadCount: number; }
