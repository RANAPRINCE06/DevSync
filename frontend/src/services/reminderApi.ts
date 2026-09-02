import { apiClient } from './client';
import { ApiResponse, PageResponse } from '@/types/api';
import {
  Reminder,
  CreateReminderRequest,
  UpdateReminderRequest,
  ReminderFilterParams,
} from '@/types/reminder';

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
    // Default endDate to 1 year ahead if omitted by user
    let endDate = data.endDate;
    if (!endDate) {
      const d = new Date(data.startDate || new Date().toISOString().split('T')[0]);
      d.setFullYear(d.getFullYear() + 1);
      endDate = d.toISOString().split('T')[0];
    }

    const payload = {
      ...data,
      endDate,
    };
    const res = await apiClient.post<ApiResponse<Reminder>>('/reminders', payload);
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
