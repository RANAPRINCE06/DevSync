export type ReminderType = 'DAILY_PROGRESS' | 'GOAL_CHECKIN' | 'TASK_DUE' | 'CUSTOM';
export type ReminderStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';

export interface Reminder {
  id: string;
  userId: string;
  userName: string;
  teamId: string;
  teamName: string;
  type: ReminderType;
  status: ReminderStatus;
  title: string;
  message?: string | null;
  reminderTime: string; // e.g. "09:00:00"
  timezone: string;
  startDate: string; // "YYYY-MM-DD"
  endDate?: string | null;
  active: boolean;
  lastTriggeredAt?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReminderRequest {
  userId: string;
  teamId: string;
  type: ReminderType;
  title: string;
  message?: string;
  reminderTime: string; // "HH:mm" or "HH:mm:ss"
  timezone: string;
  startDate: string;
  endDate?: string;
}

export interface UpdateReminderRequest {
  title?: string;
  message?: string;
  reminderTime?: string;
  timezone?: string;
  startDate?: string;
  endDate?: string;
  status?: ReminderStatus;
  active?: boolean;
}

export interface ReminderFilterParams {
  userId?: string;
  teamId?: string;
  type?: ReminderType;
  status?: ReminderStatus;
  active?: boolean;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}
