import React from 'react';
import {
  Settings,
  Moon,
  Globe,
  Shield,
  Users,
  Sparkles,
  Zap,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { useApp } from '@/contexts/AppContext';

export const SettingsPage: React.FC = () => {
  const { activeUser, activeTeam, users, teams } = useApp();

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <div className="pb-2 border-b border-slate-850">
        <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <Settings className="w-5 h-5 text-slate-400" />
          Settings & Environment Preferences
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Configure preferences, inspect active workspace details, and manage developer session context.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* User Context */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Shield className="w-4 h-4 text-primary-400" />
              <CardTitle>Active User Profile</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-xs">
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Developer Name</span>
                <span className="text-slate-100 font-semibold">{activeUser?.name || 'Developer'}</span>
              </div>
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Email</span>
                <span className="text-slate-100 font-semibold">{activeUser?.email || 'N/A'}</span>
              </div>
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Timezone</span>
                <span className="text-slate-100 font-mono text-[11px]">{activeUser?.timezone || 'UTC'}</span>
              </div>
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Total Registered Users</span>
                <span className="text-primary-400 font-bold">{users.length}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Team Workspace Context */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Users className="w-4 h-4 text-sky-400" />
              <CardTitle>Active Team Workspace</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-xs">
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Team Name</span>
                <span className="text-slate-100 font-semibold">{activeTeam?.name || 'Developer Team'}</span>
              </div>
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Description</span>
                <span className="text-slate-300 truncate max-w-xs">{activeTeam?.description || 'N/A'}</span>
              </div>
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Available Teams</span>
                <span className="text-sky-400 font-bold">{teams.length}</span>
              </div>
              <div className="flex justify-between p-2.5 rounded-xl bg-slate-950/50 border border-slate-850">
                <span className="text-slate-500">Status</span>
                <Badge variant="success" size="sm">
                  Active
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Appearance & System */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Moon className="w-4 h-4 text-indigo-400" />
              <CardTitle>Appearance & Theme</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-xs">
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800">
                <span className="flex items-center gap-2 text-slate-300">
                  <Moon className="w-4 h-4 text-primary-400" /> Theme Mode
                </span>
                <Badge variant="primary" size="sm">
                  Developer Dark (Default)
                </Badge>
              </div>
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800">
                <span className="flex items-center gap-2 text-slate-300">
                  <Sparkles className="w-4 h-4 text-amber-400" /> Accent Palette
                </span>
                <span className="text-primary-300 font-medium font-mono text-[11px]">Violet + Slate</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* API Gateway & Architecture */}
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Globe className="w-4 h-4 text-emerald-400" />
              <CardTitle>API Gateway & Engine</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-xs">
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800">
                <span className="flex items-center gap-2 text-slate-300">
                  <Globe className="w-4 h-4 text-emerald-400" /> REST API Gateway
                </span>
                <span className="text-emerald-400 font-mono text-[11px]">/api/v1</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/50 border border-slate-800">
                <span className="flex items-center gap-2 text-slate-300">
                  <Zap className="w-4 h-4 text-amber-400" /> Caching & State
                </span>
                <span className="text-slate-300 font-medium">TanStack Query v5</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
