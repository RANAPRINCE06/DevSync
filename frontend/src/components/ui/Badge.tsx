import React, { HTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'outline';
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({
  className,
  variant = 'default',
  size = 'md',
  children,
  ...props
}) => {
  const variants = {
    default: 'bg-slate-800 text-slate-300 border-slate-700/60',
    primary: 'bg-primary-950/70 text-primary-300 border-primary-800/60',
    success: 'bg-emerald-950/70 text-emerald-300 border-emerald-800/60',
    warning: 'bg-amber-950/70 text-amber-300 border-amber-800/60',
    danger: 'bg-rose-950/70 text-rose-300 border-rose-800/60',
    info: 'bg-sky-950/70 text-sky-300 border-sky-800/60',
    outline: 'bg-transparent text-slate-300 border-slate-700',
  };

  const sizes = {
    sm: 'px-2 py-0.5 text-[10px] font-medium',
    md: 'px-2.5 py-0.5 text-xs font-medium',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full border leading-none transition-colors',
        variants[variant],
        sizes[size],
        className
      )}
      {...props}
    >
      {children}
    </span>
  );
};
