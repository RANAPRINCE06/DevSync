import React from 'react';
import { Settings, Moon, Globe, Shield } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { useApp } from '@/contexts/AppContext';

export const SettingsPage: React.FC = () => {
  const { activeUser } = useApp();

  return (
    <div className="space-y-6">
      <div className="pb-2 border-b border-slate-850">
        <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <Settings className="w-5 h-5 text-slate-400" />
          Settings
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Configure preferences, notifications, theme, and profile details.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Shield className="w-4 h-4 text-primary-400" />
              <CardTitle>Profile Information</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-xs">
              <div>
                <span className="text-slate-500 block">Name</span>
                <span className="text-slate-200 font-medium">{activeUser?.name || 'Developer'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Email</span>
                <span className="text-slate-200 font-medium">{activeUser?.email || 'N/A'}</span>
              </div>
              <div>
                <span className="text-slate-500 block">Timezone</span>
                <span className="text-slate-200 font-medium">{activeUser?.timezone || 'UTC'}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Moon className="w-4 h-4 text-indigo-400" />
              <CardTitle>Appearance & Preferences</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-xs text-slate-300">
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="flex items-center gap-2">
                  <Moon className="w-4 h-4 text-primary-400" /> Dark Theme
                </span>
                <span className="text-emerald-400 font-medium">Active (Default)</span>
              </div>
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/40 border border-slate-800">
                <span className="flex items-center gap-2">
                  <Globe className="w-4 h-4 text-sky-400" /> API Gateway
                </span>
                <span className="text-slate-400 font-mono text-[11px]">/api/v1</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
