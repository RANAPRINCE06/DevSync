import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { reminderApi } from '@/services/reminderApi';
import {
  ReminderFilterParams,
  CreateReminderRequest,
  UpdateReminderRequest,
} from '@/types/reminder';

export const useReminders = (params?: ReminderFilterParams) => {
  return useQuery({
    queryKey: ['reminders', params],
    queryFn: () => reminderApi.getReminders(params),
  });
};

export const useReminder = (id?: string) => {
  return useQuery({
    queryKey: ['reminders', id],
    queryFn: () => (id ? reminderApi.getReminderById(id) : Promise.reject('No reminder ID')),
    enabled: !!id,
  });
};

export const useCreateReminder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateReminderRequest) => reminderApi.createReminder(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reminders'] });
    },
  });
};

export const useUpdateReminder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateReminderRequest }) =>
      reminderApi.updateReminder(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reminders'] });
    },
  });
};

export const useDeleteReminder = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => reminderApi.deleteReminder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reminders'] });
    },
  });
};
