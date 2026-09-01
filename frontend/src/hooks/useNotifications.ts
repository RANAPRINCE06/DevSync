import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '@/services/notificationApi';
import { CreateNotificationRequest, NotificationFilterParams } from '@/types/notification';

export const useNotifications = (params?: NotificationFilterParams) => {
  return useQuery({
    queryKey: ['notifications', params],
    queryFn: () => notificationApi.getNotifications(params),
  });
};

export const useUnreadNotifications = (params?: { page?: number; size?: number }) => {
  return useQuery({
    queryKey: ['notifications', 'unread', params],
    queryFn: () => notificationApi.getUnreadNotifications(params),
  });
};

export const useUnreadCount = (userId?: string) => {
  return useQuery({
    queryKey: ['notifications', 'unread', 'count', userId],
    queryFn: () => (userId ? notificationApi.getUnreadCount(userId) : Promise.resolve({ unreadCount: 0 })),
    enabled: !!userId,
    refetchInterval: 30000,
  });
};

export const useCreateNotification = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateNotificationRequest) => notificationApi.createNotification(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const useMarkAsRead = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};

export const useMarkAllAsRead = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) => notificationApi.markAllAsRead(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};
