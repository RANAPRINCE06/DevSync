import React from 'react';
import { TrendingUp, Plus } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';

export const ProgressPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-primary-400" />
            Daily Progress & Accountability
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Log, filter, and inspect daily developer activity and study sessions.
          </p>
        </div>
        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
          Log Progress
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Daily Progress Logs</CardTitle>
        </CardHeader>
        <CardContent>
          <EmptyState
            icon={TrendingUp}
            title="Daily Progress Module"
            description="The dedicated Daily Progress log, calendar view, and historical filter views will be extended in Step 3B."
          />
        </CardContent>
      </Card>
    </div>
  );
};
