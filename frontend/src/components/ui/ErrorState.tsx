import React from 'react';
import { AlertTriangle, RotateCw, LucideIcon } from 'lucide-react';
import { Button } from './Button';

interface ErrorStateProps {
  icon?: LucideIcon;
  title?: string;
  description?: string;
  onRetry?: () => void;
  isRetrying?: boolean;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  icon: Icon = AlertTriangle,
  title = 'Something went wrong',
  description = 'We encountered an error loading this data. Please try again.',
  onRetry,
  isRetrying = false,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 text-center rounded-2xl bg-rose-950/20 border border-rose-900/30 space-y-3">
      <div className="w-12 h-12 rounded-2xl bg-rose-950/60 border border-rose-800/60 flex items-center justify-center text-rose-400">
        <Icon className="w-6 h-6" />
      </div>
      <div className="space-y-1 max-w-sm">
        <h3 className="text-sm font-bold text-slate-100">{title}</h3>
        <p className="text-xs text-slate-400">{description}</p>
      </div>
      {onRetry && (
        <Button
          variant="outline"
          size="sm"
          onClick={onRetry}
          isLoading={isRetrying}
          leftIcon={<RotateCw className="w-3.5 h-3.5" />}
          className="border-rose-800/60 hover:bg-rose-950/40 text-rose-300"
        >
          Try Again
        </Button>
      )}
    </div>
  );
};
