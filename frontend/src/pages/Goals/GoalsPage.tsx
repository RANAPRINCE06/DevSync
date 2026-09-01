import React from 'react';
import { Target, Plus } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';

export const GoalsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Target className="w-5 h-5 text-indigo-400" />
            Learning & Development Goals
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Define, track progress percentages, and manage milestones for team goals.
          </p>
        </div>
        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
          New Goal
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Goals</CardTitle>
        </CardHeader>
        <CardContent>
          <EmptyState
            icon={Target}
            title="Goals Management Module"
            description="The dedicated Goal tracking view, progress slider, and priority filtering UI will be extended in Step 3B."
          />
        </CardContent>
      </Card>
    </div>
  );
};
