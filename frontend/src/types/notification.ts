export type NotificationType = 'DAILY_REMINDER' | 'GOAL_REMINDER' | 'TASK_REMINDER' | 'SYSTEM' | 'ACHIEVEMENT';
export type NotificationStatus = 'UNREAD' | 'READ';

export interface Notification {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  title: string;
  message: string;
  type: NotificationType;
  status: NotificationStatus;
  readAt?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateNotificationRequest {
  userId: string;
  title: string;
  message: string;
  type: NotificationType;
}

export interface NotificationFilterParams {
  userId?: string;
  type?: NotificationType;
  status?: NotificationStatus;
  page?: number;
  size?: number;
  sort?: string;
}
