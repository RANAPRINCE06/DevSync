import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import { Reminder, CreateReminderRequest, UpdateReminderRequest, ReminderFilterParams } from '@/types/reminder';

export const reminderApi = {
  getReminders: async (params?: ReminderFilterParams): Promise<PageResponse<Reminder>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<Reminder>>>('/reminders', { params });
    return res.data.data;
  },

  getReminderById: async (id: string): Promise<Reminder> => {
    const res = await apiClient.get<ApiResponse<Reminder>>(`/reminders/${id}`);
    return res.data.data;
  },

  createReminder: async (data: CreateReminderRequest): Promise<Reminder> => {
    const res = await apiClient.post<ApiResponse<Reminder>>('/reminders', data);
    return res.data.data;
  },

  updateReminder: async (id: string, data: UpdateReminderRequest): Promise<Reminder> => {
    const res = await apiClient.put<ApiResponse<Reminder>>(`/reminders/${id}`, data);
    return res.data.data;
  },

  deleteReminder: async (id: string): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/reminders/${id}`);
  },
};
