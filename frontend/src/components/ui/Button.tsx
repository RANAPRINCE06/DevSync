import React, { ButtonHTMLAttributes, ReactNode } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { Loader2 } from 'lucide-react';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'danger' | 'success' | 'ghost';
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'icon';
  isLoading?: boolean;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      className,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      disabled,
      ...props
    },
    ref
  ) => {
    const baseStyles =
      'inline-flex items-center justify-center font-semibold rounded-[3px] transition-colors focus:outline-none focus:ring-1 focus:ring-offset-1 focus:ring-blue-600 disabled:opacity-50 disabled:cursor-not-allowed shadow-xs border select-none';

    const variants = {
      primary:
        'bg-[#1d4ed8] hover:bg-[#1e40af] text-white border-[#1e40af] dark:bg-blue-600 dark:hover:bg-blue-700 dark:border-blue-700',
      secondary:
        'bg-[#f1f5f9] hover:bg-[#e2e8f0] text-[#334155] border-[#cbd5e1] dark:bg-slate-800 dark:hover:bg-slate-700 dark:text-slate-200 dark:border-slate-600',
      outline:
        'bg-white hover:bg-[#f8fafc] text-[#334155] border-[#cbd5e1] dark:bg-slate-900 dark:hover:bg-slate-800 dark:text-slate-200 dark:border-slate-700',
      danger:
        'bg-[#dc2626] hover:bg-[#b91c1c] text-white border-[#b91c1c] dark:bg-red-600 dark:hover:bg-red-700',
      success:
        'bg-[#16a34a] hover:bg-[#15803d] text-white border-[#15803d] dark:bg-emerald-600 dark:hover:bg-emerald-700',
      ghost:
        'bg-transparent hover:bg-slate-100 text-[#334155] border-transparent shadow-none dark:hover:bg-slate-800 dark:text-slate-300',
    };

    const sizes = {
      xs: 'px-2 py-0.5 text-[11px] gap-1',
      sm: 'px-2.5 py-1 text-xs gap-1.5',
      md: 'px-3 py-1.5 text-xs gap-2',
      lg: 'px-4 py-2 text-sm gap-2',
      icon: 'p-1.5 text-xs gap-0',
    };

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={twMerge(clsx(baseStyles, variants[variant], sizes[size], className))}
        {...props}
      >
        {isLoading ? (
          <Loader2 className="w-3.5 h-3.5 animate-spin" />
        ) : (
          leftIcon && <span className="inline-flex shrink-0">{leftIcon}</span>
        )}
        {children}
        {!isLoading && rightIcon && <span className="inline-flex shrink-0">{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';
