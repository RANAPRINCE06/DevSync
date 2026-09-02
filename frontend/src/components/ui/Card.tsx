import React, { HTMLAttributes } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'flat';
  hoverable?: boolean;
}

export const Card: React.FC<CardProps> = ({ children, className, hoverable, ...props }) => {
  return (
    <div
      className={twMerge(
        clsx(
          'bg-white dark:bg-[#111827] border border-[#cfd5dc] dark:border-slate-800 rounded-[3px] shadow-xs overflow-hidden',
          hoverable && 'hover:border-blue-500 transition-colors',
          className
        )
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export const CardHeader: React.FC<HTMLAttributes<HTMLDivElement>> = ({
  children,
  className,
  ...props
}) => {
  return (
    <div
      className={twMerge(
        clsx(
          'bg-[#f8fafc] dark:bg-slate-800/80 border-b border-[#cfd5dc] dark:border-slate-800 px-3.5 py-2 flex items-center justify-between',
          className
        )
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export const CardTitle: React.FC<HTMLAttributes<HTMLHeadingElement>> = ({
  children,
  className,
  ...props
}) => {
  return (
    <h3
      className={twMerge(
        clsx('text-xs font-bold uppercase tracking-wider text-slate-800 dark:text-slate-200', className)
      )}
      {...props}
    >
      {children}
    </h3>
  );
};

export const CardContent: React.FC<HTMLAttributes<HTMLDivElement>> = ({
  children,
  className,
  ...props
}) => {
  return <div className={twMerge(clsx('p-3.5', className))} {...props}>{children}</div>;
};

export const CardFooter: React.FC<HTMLAttributes<HTMLDivElement>> = ({
  children,
  className,
  ...props
}) => {
  return (
    <div
      className={twMerge(
        clsx(
          'bg-[#f8fafc] dark:bg-slate-800/60 border-t border-[#cfd5dc] dark:border-slate-800 px-3.5 py-2.5',
          className
        )
      )}
      {...props}
    >
      {children}
    </div>
  );
};
