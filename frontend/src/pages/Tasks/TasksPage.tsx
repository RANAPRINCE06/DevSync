import React from 'react';
import { CheckSquare, Plus } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';

export const TasksPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <CheckSquare className="w-5 h-5 text-emerald-400" />
            Task Management
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Organize actionable tasks mapped directly to your learning goals.
          </p>
        </div>
        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
          New Task
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Tasks</CardTitle>
        </CardHeader>
        <CardContent>
          <EmptyState
            icon={CheckSquare}
            title="Task Management Module"
            description="The dedicated Kanban/List view, status toggles, and time tracking UI will be extended in Step 3B."
          />
        </CardContent>
      </Card>
    </div>
  );
};
