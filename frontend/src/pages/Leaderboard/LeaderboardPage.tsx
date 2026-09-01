import React from 'react';
import { Trophy } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';

export const LeaderboardPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Trophy className="w-5 h-5 text-amber-400" />
            Team Leaderboard
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time rankings based on daily consistency, task delivery, and achievements.
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Standings</CardTitle>
        </CardHeader>
        <CardContent>
          <EmptyState
            icon={Trophy}
            title="Team Leaderboard Module"
            description="The dedicated period filters (Daily, Weekly, Monthly, All-time) and animated score breakdown views will be extended in Step 3B."
          />
        </CardContent>
      </Card>
    </div>
  );
};
