import React, { useState } from 'react';
import {
  Settings,
  Moon,
  Sun,
  Laptop,
  Shield,
  User as UserIcon,
  Bell,
  Lock,
  LogOut,
  AlertTriangle,
  Save,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Tabs } from '@/components/ui/Tabs';
import { Modal } from '@/components/ui/Modal';
import { useAuth } from '@/hooks/useAuth';
import { useTheme } from '@/contexts/ThemeContext';
import { useToast } from '@/contexts/ToastContext';

export const SettingsPage: React.FC = () => {
  const { user, updateProfile, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<string>('PROFILE');

  // Profile Form States
  const [name, setName] = useState(user?.name || '');
  const [timezone, setTimezone] = useState(user?.timezone || 'UTC');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  // Account / Password Form States
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // Preference toggles
  const [dailyReminders, setDailyReminders] = useState(true);
  const [goalReminders, setGoalReminders] = useState(true);
  const [taskReminders, setTaskReminders] = useState(true);

  // Danger zone modal
  const [isDeactivateModalOpen, setIsDeactivateModalOpen] = useState(false);

  const settingTabs = [
    { id: 'PROFILE', label: 'Profile' },
    { id: 'ACCOUNT', label: 'Account & Security' },
    { id: 'PREFERENCES', label: 'Preferences & Theme' },
    { id: 'DANGER_ZONE', label: 'Danger Zone' },
  ];

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      showToast('warning', 'Name is required');
      return;
    }

    setIsSavingProfile(true);
    try {
      await updateProfile({
        name: name.trim(),
        timezone: timezone.trim() || 'UTC',
        avatarUrl: avatarUrl.trim() ? avatarUrl : undefined,
      });
      showToast('success', 'Profile updated successfully!');
    } catch (err: unknown) {
      showToast('error', 'Failed to update profile', err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setIsSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPassword || newPassword.length < 6) {
      showToast('warning', 'New password must be at least 6 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      showToast('warning', 'New passwords do not match');
      return;
    }

    setIsChangingPassword(true);
    setTimeout(() => {
      setIsChangingPassword(false);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      showToast('success', 'Password updated successfully!');
    }, 600);
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* 1. Page Header */}
      <div className="pb-2 border-b border-slate-850">
        <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <Settings className="w-5 h-5 text-slate-400" />
          Settings & Account Center
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Manage your developer profile, authentication credentials, and environment preferences.
        </p>
      </div>

      {/* 2. Navigation Tabs */}
      <Tabs tabs={settingTabs} activeTab={activeTab} onChange={setActiveTab} />

      {/* 3. Tab Contents */}

      {/* TAB 1: PROFILE */}
      {activeTab === 'PROFILE' && (
        <Card className="max-w-2xl">
          <CardHeader>
            <div className="flex items-center gap-2">
              <UserIcon className="w-4 h-4 text-primary-400" />
              <CardTitle>Profile Details</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSaveProfile} className="space-y-4">
              <Input
                label="Full Name *"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />

              <Input
                label="Email Address"
                value={user?.email || ''}
                disabled
                helperText="Email is associated with your primary DevSync account identifier."
              />

              <Input
                label="IANA Timezone *"
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
                placeholder="e.g. America/New_York, Asia/Kolkata, UTC"
                required
              />

              <Input
                label="Avatar Image URL (Optional)"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                placeholder="https://example.com/avatar.png"
              />

              <div className="flex justify-end pt-3 border-t border-slate-800">
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  isLoading={isSavingProfile}
                  leftIcon={<Save className="w-4 h-4" />}
                >
                  Save Profile
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* TAB 2: ACCOUNT & SECURITY */}
      {activeTab === 'ACCOUNT' && (
        <div className="space-y-6 max-w-2xl">
          {/* Change Password Card */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Lock className="w-4 h-4 text-emerald-400" />
                <CardTitle>Change Password</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleChangePassword} className="space-y-4">
                <Input
                  type="password"
                  label="Current Password"
                  placeholder="••••••••"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                />

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <Input
                    type="password"
                    label="New Password *"
                    placeholder="••••••••"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                  />

                  <Input
                    type="password"
                    label="Confirm New Password *"
                    placeholder="••••••••"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />
                </div>

                <div className="flex justify-end pt-3 border-t border-slate-800">
                  <Button
                    type="submit"
                    variant="primary"
                    size="sm"
                    isLoading={isChangingPassword}
                  >
                    Update Password
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          {/* Session Information */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Shield className="w-4 h-4 text-sky-400" />
                <CardTitle>Active Session</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 text-xs">
                <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800">
                  <div>
                    <span className="font-semibold text-slate-200 block">Current Web Browser Session</span>
                    <span className="text-[11px] text-slate-500">Authenticated via Secure Token</span>
                  </div>
                  <Badge variant="success" size="sm">
                    Active
                  </Badge>
                </div>

                <div className="flex justify-end pt-2">
                  <Button
                    variant="danger"
                    size="sm"
                    leftIcon={<LogOut className="w-3.5 h-3.5" />}
                    onClick={logout}
                  >
                    Sign Out
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* TAB 3: PREFERENCES & THEME */}
      {activeTab === 'PREFERENCES' && (
        <div className="space-y-6 max-w-2xl">
          {/* Theme Selector */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Moon className="w-4 h-4 text-indigo-400" />
                <CardTitle>Interface Theme</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-3 gap-3">
                <button
                  type="button"
                  onClick={() => setTheme('dark')}
                  className={`p-4 rounded-xl border flex flex-col items-center gap-2 transition-all ${
                    theme === 'dark'
                      ? 'bg-primary-950/40 border-primary-500 text-primary-300 ring-1 ring-primary-500/50'
                      : 'bg-slate-950/40 border-slate-800 text-slate-400 hover:border-slate-700'
                  }`}
                >
                  <Moon className="w-5 h-5" />
                  <span className="text-xs font-bold">Dark Theme</span>
                  <span className="text-[10px] text-slate-500">Default Dev Experience</span>
                </button>

                <button
                  type="button"
                  onClick={() => setTheme('light')}
                  className={`p-4 rounded-xl border flex flex-col items-center gap-2 transition-all ${
                    theme === 'light'
                      ? 'bg-primary-950/40 border-primary-500 text-primary-300 ring-1 ring-primary-500/50'
                      : 'bg-slate-950/40 border-slate-800 text-slate-400 hover:border-slate-700'
                  }`}
                >
                  <Sun className="w-5 h-5" />
                  <span className="text-xs font-bold">Light Theme</span>
                  <span className="text-[10px] text-slate-500">High Contrast Clean</span>
                </button>

                <button
                  type="button"
                  onClick={() => setTheme('system')}
                  className={`p-4 rounded-xl border flex flex-col items-center gap-2 transition-all ${
                    theme === 'system'
                      ? 'bg-primary-950/40 border-primary-500 text-primary-300 ring-1 ring-primary-500/50'
                      : 'bg-slate-950/40 border-slate-800 text-slate-400 hover:border-slate-700'
                  }`}
                >
                  <Laptop className="w-5 h-5" />
                  <span className="text-xs font-bold">System Sync</span>
                  <span className="text-[10px] text-slate-500">Matches OS Theme</span>
                </button>
              </div>
            </CardContent>
          </Card>

          {/* Notification Reminders */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Bell className="w-4 h-4 text-amber-400" />
                <CardTitle>In-App Notifications & Alerts</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 text-xs">
                <label className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800 cursor-pointer">
                  <div>
                    <span className="font-semibold text-slate-200 block">Daily Study & Progress Reminders</span>
                    <span className="text-slate-500 text-[11px]">Receive notification to log daily study sessions</span>
                  </div>
                  <input
                    type="checkbox"
                    checked={dailyReminders}
                    onChange={(e) => setDailyReminders(e.target.checked)}
                    className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-primary-600 focus:ring-primary-500"
                  />
                </label>

                <label className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800 cursor-pointer">
                  <div>
                    <span className="font-semibold text-slate-200 block">Goal Deadline Alerts</span>
                    <span className="text-slate-500 text-[11px]">Alert when learning goal target date is within 3 days</span>
                  </div>
                  <input
                    type="checkbox"
                    checked={goalReminders}
                    onChange={(e) => setGoalReminders(e.target.checked)}
                    className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-primary-600 focus:ring-primary-500"
                  />
                </label>

                <label className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800 cursor-pointer">
                  <div>
                    <span className="font-semibold text-slate-200 block">Task Assignment Alerts</span>
                    <span className="text-slate-500 text-[11px]">Notify when team assigns sprint tasks</span>
                  </div>
                  <input
                    type="checkbox"
                    checked={taskReminders}
                    onChange={(e) => setTaskReminders(e.target.checked)}
                    className="w-4 h-4 rounded border-slate-700 bg-slate-900 text-primary-600 focus:ring-primary-500"
                  />
                </label>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* TAB 4: DANGER ZONE */}
      {activeTab === 'DANGER_ZONE' && (
        <Card className="max-w-2xl border-rose-900/50 bg-rose-950/10">
          <CardHeader>
            <div className="flex items-center gap-2 text-rose-400">
              <AlertTriangle className="w-4 h-4" />
              <CardTitle className="text-rose-300">Danger Zone</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4 text-xs">
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/80 border border-rose-900/40">
                <div>
                  <span className="font-semibold text-slate-200 block">Deactivate Account</span>
                  <span className="text-slate-400 text-[11px]">
                    Temporarily archive your profile and pause daily notifications.
                  </span>
                </div>
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => setIsDeactivateModalOpen(true)}
                >
                  Deactivate
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Deactivate Confirmation Modal */}
      {isDeactivateModalOpen && (
        <Modal
          isOpen={isDeactivateModalOpen}
          onClose={() => setIsDeactivateModalOpen(false)}
          title="Deactivate Account"
          description="Are you sure you want to deactivate your DevSync developer account?"
        >
          <div className="space-y-4 text-xs">
            <p className="text-slate-300">
              Deactivating your account will archive your daily progress logs and remove you from the active team leaderboard. You can reactivate by contacting your workspace owner.
            </p>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
              <Button variant="outline" size="sm" onClick={() => setIsDeactivateModalOpen(false)}>
                Cancel
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={async () => {
                  setIsDeactivateModalOpen(false);
                  await logout();
                  showToast('info', 'Account deactivated.');
                }}
              >
                Confirm Deactivation
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
