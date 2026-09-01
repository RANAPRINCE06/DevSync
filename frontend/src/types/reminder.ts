export type ReminderType = 'DAILY_PROGRESS' | 'GOAL' | 'TASK';
export type ReminderStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';

export interface Reminder {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  teamId: string;
  teamName: string;
  title: string;
  message?: string | null;
  type: ReminderType;
  status: ReminderStatus;
  reminderTime: string;
  timezone: string;
  startDate: string;
  endDate?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReminderRequest {
  userId: string;
  teamId: string;
  title: string;
  message?: string;
  type: ReminderType;
  reminderTime: string;
  timezone: string;
  startDate: string;
  endDate?: string;
}

export interface UpdateReminderRequest {
  title: string;
  message?: string;
  type: ReminderType;
  status?: ReminderStatus;
  reminderTime: string;
  timezone: string;
  startDate: string;
  endDate?: string;
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
