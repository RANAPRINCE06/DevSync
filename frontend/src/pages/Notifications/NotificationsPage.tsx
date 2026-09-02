import React, { useState } from 'react';
import { Bell, Check, CheckCheck, Trash2 } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import {
  useNotifications,
  useMarkAsRead,
  useMarkAllAsRead,
  useDeleteNotification,
} from '@/hooks/useNotifications';
import { formatDate } from '@/lib/utils';
import { NotificationType } from '@/types/notification';

export const NotificationsPage: React.FC = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<'ALL' | 'UNREAD' | NotificationType>('ALL');

  const { data: notificationsPage, isLoading } = useNotifications({
    userId: user?.id,
    size: 1000,
  });

  const markAsReadMutation = useMarkAsRead();
  const markAllAsReadMutation = useMarkAllAsRead();
  const deleteNotificationMutation = useDeleteNotification();

  const allNotifications = notificationsPage?.content || [];

  const filteredNotifications = allNotifications.filter((n) => {
    if (activeTab === 'ALL') return true;
    if (activeTab === 'UNREAD') return n.status === 'UNREAD';
    return n.type === activeTab;
  });

  const unreadCount = allNotifications.filter((n) => n.status === 'UNREAD').length;

  const handleMarkAsRead = async (id: string) => {
    try {
      await markAsReadMutation.mutateAsync(id);
      showToast('success', 'Notification marked as read');
    } catch {
      showToast('error', 'Failed to update notification');
    }
  };

  const handleMarkAllRead = async () => {
    if (!user) return;
    try {
      await markAllAsReadMutation.mutateAsync(user.id);
      showToast('success', 'All notifications marked as read');
    } catch {
      showToast('error', 'Failed to mark all as read');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteNotificationMutation.mutateAsync(id);
      showToast('success', 'Notification deleted');
    } catch {
      showToast('error', 'Failed to delete notification');
    }
  };

  const tabs: { label: string; value: 'ALL' | 'UNREAD' | NotificationType }[] = [
    { label: 'All Alerts', value: 'ALL' },
    { label: `Unread (${unreadCount})`, value: 'UNREAD' },
    { label: 'Daily Reminders', value: 'DAILY_REMINDER' },
    { label: 'Goal Check-ins', value: 'GOAL_REMINDER' },
    { label: 'Task Alerts', value: 'TASK_REMINDER' },
    { label: 'System', value: 'SYSTEM' },
    { label: 'Achievements', value: 'ACHIEVEMENT' },
  ];

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Bell className="w-4 h-4 text-blue-600" />
            Notification Feed & System Alerts
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            {unreadCount} unread notifications • Daily standup reminders, goal check-ins, and task updates
          </p>
        </div>

        {unreadCount > 0 && (
          <Button
            variant="outline"
            size="sm"
            leftIcon={<CheckCheck className="w-3.5 h-3.5 text-blue-600" />}
            onClick={handleMarkAllRead}
            isLoading={markAllAsReadMutation.isPending}
          >
            Mark All as Read
          </Button>
        )}
      </div>

      {/* 2. Filter Tabs */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-2 rounded-[3px] flex items-center gap-1.5 overflow-x-auto text-xs">
        {tabs.map((t) => (
          <button
            key={t.value}
            onClick={() => setActiveTab(t.value)}
            className={`px-3 py-1 font-semibold rounded-[2px] whitespace-nowrap transition-colors ${
              activeTab === t.value
                ? 'bg-blue-600 text-white'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* 3. Notifications List */}
      <Card>
        <CardHeader>
          <CardTitle>Alerts History</CardTitle>
          <span className="text-[11px] text-slate-500">{filteredNotifications.length} Notifications</span>
        </CardHeader>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          ) : filteredNotifications.length > 0 ? (
            <div className="divide-y divide-slate-200 dark:divide-slate-800">
              {filteredNotifications.map((n) => (
                <div
                  key={n.id}
                  className={`p-3 flex items-start justify-between gap-3 text-xs transition-colors ${
                    n.status === 'UNREAD'
                      ? 'bg-blue-50/50 dark:bg-blue-950/20'
                      : 'hover:bg-slate-50 dark:hover:bg-slate-800/40'
                  }`}
                >
                  <div className="flex items-start gap-2.5 min-w-0 flex-1">
                    <div
                      className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${
                        n.status === 'UNREAD' ? 'bg-blue-600' : 'bg-transparent'
                      }`}
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-slate-900 dark:text-slate-100">{n.title}</span>
                        <Badge size="sm" variant="default">
                          {n.type}
                        </Badge>
                      </div>
                      <p className="text-slate-600 dark:text-slate-400 mt-0.5">{n.message}</p>
                      <span className="text-[10px] text-slate-400 mt-1 block">{formatDate(n.createdAt)}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-1 shrink-0">
                    {n.status === 'UNREAD' && (
                      <button
                        onClick={() => handleMarkAsRead(n.id)}
                        className="p-1 text-blue-600 hover:bg-blue-100 rounded-[2px] font-semibold text-[11px]"
                        title="Mark as read"
                      >
                        <Check className="w-3.5 h-3.5" />
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(n.id)}
                      className="p-1 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-[2px]"
                      title="Delete"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-6">
              <EmptyState
                icon={Bell}
                title="No notifications"
                description="You are all caught up. New reminders and team updates will appear here."
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
