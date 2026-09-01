import React from 'react';
import { Medal } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { EmptyState } from '@/components/ui/EmptyState';

export const AchievementsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Medal className="w-5 h-5 text-primary-400" />
            Badges & Achievements
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Celebrate streaks, problem-solving milestones, and personal bests.
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Earned Badges</CardTitle>
        </CardHeader>
        <CardContent>
          <EmptyState
            icon={Medal}
            title="Achievements Module"
            description="The dedicated badge gallery, tier unlocks, and milestone celebration view will be extended in Step 3B."
          />
        </CardContent>
      </Card>
    </div>
  );
};
