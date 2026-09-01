import React, { useState } from 'react';
import {
  Bell,
  CheckCheck,
  CheckCircle2,
  Trash2,
  Target,
  Calendar,
  AlertCircle,
  Clock,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Tabs } from '@/components/ui/Tabs';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { Pagination } from '@/components/ui/Pagination';
import {
  useNotifications,
  useMarkAsRead,
  useMarkAllAsRead,
  useDeleteNotification,
} from '@/hooks/useNotifications';
import { formatDate } from '@/lib/utils';
import { Notification, NotificationType } from '@/types/notification';

export const NotificationsPage: React.FC = () => {
  const { activeUser } = useApp();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<string>('ALL');
  const [page, setPage] = useState(0);

  const { data: notificationsPage, isLoading } = useNotifications({
    userId: activeUser?.id,
    status: activeTab === 'UNREAD' ? 'UNREAD' : activeTab === 'READ' ? 'READ' : undefined,
    page,
    size: 15,
    sort: 'createdAt,desc',
  });

  const markAsReadMutation = useMarkAsRead();
  const markAllMutation = useMarkAllAsRead();
  const deleteMutation = useDeleteNotification();

  const filterTabs = [
    { id: 'ALL', label: 'All Alerts' },
    { id: 'UNREAD', label: 'Unread' },
    { id: 'READ', label: 'Archived / Read' },
  ];

  const handleMarkOneRead = async (notification: Notification) => {
    try {
      await markAsReadMutation.mutateAsync(notification.id);
      showToast('success', 'Notification marked as read');
    } catch (err: unknown) {
      showToast('error', 'Failed to mark as read', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleMarkAllRead = async () => {
    if (!activeUser) return;
    try {
      await markAllMutation.mutateAsync(activeUser.id);
      showToast('success', 'All notifications marked as read');
    } catch (err: unknown) {
      showToast('error', 'Failed to mark all as read', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteMutation.mutateAsync(id);
      showToast('success', 'Notification deleted');
    } catch (err: unknown) {
      showToast('error', 'Failed to delete notification', err instanceof Error ? err.message : 'Unknown error');
    }
  };

  const getTypeIcon = (type: NotificationType) => {
    switch (type) {
      case 'DAILY_REMINDER':
        return <Clock className="w-4 h-4 text-primary-400" />;
      case 'GOAL_REMINDER':
        return <Target className="w-4 h-4 text-indigo-400" />;
      case 'TASK_REMINDER':
        return <CheckCircle2 className="w-4 h-4 text-emerald-400" />;
      case 'SYSTEM':
      default:
        return <AlertCircle className="w-4 h-4 text-sky-400" />;
    }
  };

  const notifications = notificationsPage?.content || [];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Bell className="w-5 h-5 text-primary-400" />
            Notifications & Activity Alerts
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Stay on top of team milestones, sprint deadlines, and daily reminders.
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          leftIcon={<CheckCheck className="w-4 h-4" />}
          onClick={handleMarkAllRead}
          isLoading={markAllMutation.isPending}
          disabled={!activeUser || notifications.length === 0}
        >
          Mark all as read
        </Button>
      </div>

      {/* 2. Filter Tabs */}
      <Tabs
        tabs={filterTabs}
        activeTab={activeTab}
        onChange={(tabId) => {
          setActiveTab(tabId);
          setPage(0);
        }}
      />

      {/* 3. Notifications List */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <CardTitle>Notification Stream</CardTitle>
            <span className="text-xs text-slate-500">
              {notificationsPage?.totalElements || 0} total notifications
            </span>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
              <Skeleton className="h-16 w-full" />
            </div>
          ) : notifications.length > 0 ? (
            <div className="space-y-2.5">
              {notifications.map((n) => {
                const isUnread = n.status === 'UNREAD';
                return (
                  <div
                    key={n.id}
                    className={`p-4 rounded-xl border transition-all flex items-start justify-between gap-3 ${
                      isUnread
                        ? 'bg-slate-900/90 border-primary-800/40 shadow-sm'
                        : 'bg-slate-950/30 border-slate-800/60 opacity-80'
                    }`}
                  >
                    <div className="flex items-start gap-3 min-w-0">
                      <div className="p-2 rounded-xl bg-slate-950 border border-slate-800 shrink-0 mt-0.5">
                        {getTypeIcon(n.type)}
                      </div>

                      <div className="space-y-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <h4
                            className={`text-xs font-semibold ${
                              isUnread ? 'text-slate-100' : 'text-slate-300'
                            }`}
                          >
                            {n.title}
                          </h4>
                          {isUnread && (
                            <span className="w-2 h-2 rounded-full bg-primary-500 shrink-0" />
                          )}
                        </div>

                        <p className="text-xs text-slate-400 leading-relaxed">{n.message}</p>

                        <div className="flex items-center gap-3 text-[10px] text-slate-500 pt-1">
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3 h-3" />
                            {formatDate(n.createdAt)}
                          </span>
                          <span>•</span>
                          <span className="uppercase font-mono tracking-wider">{n.type}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1 shrink-0">
                      {isUnread && (
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleMarkOneRead(n)}
                          title="Mark as read"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5 text-primary-400" />
                        </Button>
                      )}
                      <Button
                        variant="ghost"
                        size="icon"
                        className="text-rose-400"
                        onClick={() => handleDelete(n.id)}
                        title="Delete notification"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <EmptyState
              icon={Bell}
              title="No notifications"
              description="You are completely caught up! New reminders and team milestones will appear here."
            />
          )}

          {notificationsPage && notificationsPage.totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={notificationsPage.totalPages}
              totalElements={notificationsPage.totalElements}
              onPageChange={setPage}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
};
