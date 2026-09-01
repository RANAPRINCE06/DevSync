import React from 'react';
import { Code2, Plus } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';

export const CodingProfilesPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2 border-b border-slate-850">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Code2 className="w-5 h-5 text-indigo-400" />
            Coding Profiles & Competitive Stats
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Connect LeetCode, Codeforces, CodeChef, and GitHub developer profiles.
          </p>
        </div>
        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
          Link Profile
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Connected Platforms</CardTitle>
        </CardHeader>
        <CardContent>
          <EmptyState
            icon={Code2}
            title="Coding Profiles Module"
            description="The dedicated competitive stats cards, rating history, and contest participation UI will be extended in Step 3B."
          />
        </CardContent>
      </Card>
    </div>
  );
};
