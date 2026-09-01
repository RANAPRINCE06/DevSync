import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import { Notification, CreateNotificationRequest, NotificationFilterParams } from '@/types/notification';

export const notificationApi = {
  getNotifications: async (params?: NotificationFilterParams): Promise<PageResponse<Notification>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Notification>>>('/notifications', { params });
    return res.data.data;
  },

  getNotificationById: async (id: string): Promise<Notification> => {
    const res = await apiClient.get<ApiResponse<Notification>>(`/notifications/${id}`);
    return res.data.data;
  },

  getUnreadNotifications: async (params?: { page?: number; size?: number; sort?: string }): Promise<PageResponse<Notification>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Notification>>>('/notifications/unread', { params });
    return res.data.data;
  },

  getUnreadCount: async (userId: string): Promise<{ unreadCount: number }> => {
    const res = await apiClient.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread/count', {
      params: { userId },
    });
    return res.data.data;
  },

  createNotification: async (data: CreateNotificationRequest): Promise<Notification> => {
    const res = await apiClient.post<ApiResponse<Notification>>('/notifications', data);
    return res.data.data;
  },

  markAsRead: async (id: string): Promise<Notification> => {
    const res = await apiClient.patch<ApiResponse<Notification>>(`/notifications/${id}/read`);
    return res.data.data;
  },

  markAllAsRead: async (userId: string): Promise<{ updatedCount: number }> => {
    const res = await apiClient.patch<ApiResponse<{ updatedCount: number }>>('/notifications/read-all', null, {
      params: { userId },
    });
    return res.data.data;
  },

  deleteNotification: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/notifications/${id}`);
  },
};
