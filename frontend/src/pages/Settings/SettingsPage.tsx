import React, { useState } from 'react';
import { Settings, User as UserIcon, Shield, Palette, Sun, Moon, Laptop, Save } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useTheme } from '@/contexts/ThemeContext';
import { useToast } from '@/contexts/ToastContext';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';

export const SettingsPage: React.FC = () => {
  const { user, updateProfile } = useAuth();
  const { theme, setTheme } = useTheme();
  const { showToast } = useToast();

  const [activeTab, setActiveTab] = useState<'PROFILE' | 'PREFERENCES' | 'SECURITY'>('PROFILE');

  // Profile Form State
  const [name, setName] = useState(user?.name || '');
  const [timezone, setTimezone] = useState(user?.timezone || 'UTC');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
  const [isUpdating, setIsUpdating] = useState(false);

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    try {
      setIsUpdating(true);
      await updateProfile({
        name: name.trim(),
        timezone,
        avatarUrl: avatarUrl.trim() || undefined,
      });
      showToast('success', 'Profile information updated successfully!');
    } catch (err: unknown) {
      showToast('error', 'Failed to update profile', err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setIsUpdating(false);
    }
  };

  const tabs = [
    { id: 'PROFILE', label: 'User Profile & Identity', icon: UserIcon },
    { id: 'PREFERENCES', label: 'Display & Preferences', icon: Palette },
    { id: 'SECURITY', label: 'Account & Security', icon: Shield },
  ];

  return (
    <div className="space-y-4">
      {/* 1. Header */}
      <div className="bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-800 p-3.5 rounded-[3px] shadow-xs">
        <h1 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
          <Settings className="w-4 h-4 text-blue-600" />
          Account & Portal Settings
        </h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
          Manage your personal profile, team preferences, and display settings
        </p>
      </div>

      {/* 2. Tabs */}
      <div className="flex border-b border-[#cfd5dc] dark:border-slate-800 bg-white dark:bg-slate-900 px-3 pt-2 text-xs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as typeof activeTab)}
            className={`px-4 py-2 font-bold border-b-2 flex items-center gap-1.5 transition-colors ${
              activeTab === tab.id
                ? 'border-blue-600 text-blue-600 dark:text-blue-400 bg-blue-50/30'
                : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            <tab.icon className="w-3.5 h-3.5" />
            {tab.label}
          </button>
        ))}
      </div>

      {/* 3. Tab Contents */}
      {activeTab === 'PROFILE' && (
        <Card>
          <CardHeader>
            <CardTitle>Profile Details</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleUpdateProfile} className="max-w-xl space-y-3">
              <Input
                label="Full Name *"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />

              <Input
                label="Email Address (Read-only)"
                value={user?.email || ''}
                disabled
                helperText="Email cannot be changed after registration."
              />

              <Input
                label="Timezone *"
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
                placeholder="e.g. UTC, America/New_York, Asia/Kolkata"
                required
              />

              <Input
                label="Avatar Image URL (Optional)"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                placeholder="https://example.com/avatar.jpg"
              />

              <div className="pt-3 border-t border-slate-200 dark:border-slate-700">
                <Button
                  type="submit"
                  variant="primary"
                  size="sm"
                  leftIcon={<Save className="w-3.5 h-3.5" />}
                  isLoading={isUpdating}
                >
                  Save Profile Changes
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {activeTab === 'PREFERENCES' && (
        <Card>
          <CardHeader>
            <CardTitle>Display & Theme Preferences</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 max-w-xl text-xs">
            <div>
              <label className="font-bold text-slate-800 dark:text-slate-200 block mb-2">Portal Color Theme</label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  type="button"
                  onClick={() => setTheme('light')}
                  className={`p-3 border rounded-[2px] text-center font-bold flex flex-col items-center gap-1.5 transition-colors ${
                    theme === 'light'
                      ? 'border-blue-600 bg-blue-50 text-blue-800'
                      : 'border-slate-300 dark:border-slate-700'
                  }`}
                >
                  <Sun className="w-4 h-4 text-amber-500" />
                  Light Mode
                </button>

                <button
                  type="button"
                  onClick={() => setTheme('dark')}
                  className={`p-3 border rounded-[2px] text-center font-bold flex flex-col items-center gap-1.5 transition-colors ${
                    theme === 'dark'
                      ? 'border-blue-600 bg-blue-950/40 text-blue-300'
                      : 'border-slate-300 dark:border-slate-700'
                  }`}
                >
                  <Moon className="w-4 h-4 text-blue-400" />
                  Dark Mode
                </button>

                <button
                  type="button"
                  onClick={() => setTheme('system')}
                  className={`p-3 border rounded-[2px] text-center font-bold flex flex-col items-center gap-1.5 transition-colors ${
                    theme === 'system'
                      ? 'border-blue-600 bg-blue-50 text-blue-800'
                      : 'border-slate-300 dark:border-slate-700'
                  }`}
                >
                  <Laptop className="w-4 h-4 text-slate-500" />
                  System Default
                </button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {activeTab === 'SECURITY' && (
        <Card>
          <CardHeader>
            <CardTitle>Account Status</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 max-w-xl text-xs">
            <div className="p-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-[2px]">
              <div className="flex items-center justify-between">
                <span className="font-bold text-slate-900 dark:text-slate-100">Account ID</span>
                <Badge size="sm" variant="default">
                  {user?.id}
                </Badge>
              </div>
              <div className="text-[11px] text-slate-500 mt-1">Status: Active verified developer</div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};
