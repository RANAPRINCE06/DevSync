import React from 'react';
import { Bell, CheckCheck } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { useApp } from '@/contexts/AppContext';
import { useNotifications, useMarkAllAsRead } from '@/hooks/useNotifications';
import { useToast } from '@/contexts/ToastContext';

export const NotificationsPage: React.FC = () => {
  const { activeUser } = useApp();
  const { showToast } = useToast();
  const { data: notificationsPage, isLoading } = useNotifications({
    userId: activeUser?.id,
    size: 20,
  });

  const markAllMutation = useMarkAllAsRead();

  const handleMarkAllRead = async () => {
    if (!activeUser) return;
    try {
      await markAllMutation.mutateAsync(activeUser.id);
      showToast('success', 'All notifications marked as read');
    } catch {
      showToast('error', 'Failed to mark all as read');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Bell className="w-5 h-5 text-primary-400" />
            Notifications & Activity Feed
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Stay updated with team milestones, task reminders, and achievements.
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          leftIcon={<CheckCheck className="w-4 h-4" />}
          onClick={handleMarkAllRead}
          isLoading={markAllMutation.isPending}
          disabled={!activeUser}
        >
          Mark all as read
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Recent Alerts</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-3">
              <div className="h-12 bg-slate-800/40 rounded-xl animate-pulse" />
              <div className="h-12 bg-slate-800/40 rounded-xl animate-pulse" />
            </div>
          ) : notificationsPage?.content && notificationsPage.content.length > 0 ? (
            <div className="space-y-2">
              {notificationsPage.content.map((n) => (
                <div
                  key={n.id}
                  className="p-3.5 rounded-xl bg-slate-950/40 border border-slate-800/80 flex items-start justify-between gap-3"
                >
                  <div>
                    <p className="text-xs font-semibold text-slate-100">{n.title}</p>
                    <p className="text-xs text-slate-400 mt-0.5">{n.message}</p>
                  </div>
                  {n.status === 'UNREAD' && (
                    <span className="w-2 h-2 rounded-full bg-primary-500 shrink-0 mt-1.5" />
                  )}
                </div>
              ))}
            </div>
          ) : (
            <EmptyState
              icon={Bell}
              title="No notifications"
              description="You are completely caught up! New reminders and team milestones will appear here."
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
};
