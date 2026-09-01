import React, { ButtonHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';
import { Loader2 } from 'lucide-react';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg' | 'icon';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      disabled,
      children,
      ...props
    },
    ref
  ) => {
    const variants = {
      primary: 'bg-primary-600 hover:bg-primary-500 text-white shadow-sm shadow-primary-900/50 focus-visible:ring-primary-500',
      secondary: 'bg-slate-800 hover:bg-slate-700 text-slate-100 border border-slate-700/60 focus-visible:ring-slate-400',
      outline: 'border border-slate-700 bg-transparent hover:bg-slate-800/80 text-slate-200 focus-visible:ring-slate-400',
      ghost: 'bg-transparent hover:bg-slate-800/60 text-slate-300 hover:text-slate-100 focus-visible:ring-slate-400',
      danger: 'bg-rose-600 hover:bg-rose-500 text-white shadow-sm shadow-rose-900/50 focus-visible:ring-rose-500',
    };

    const sizes = {
      sm: 'h-8 px-3 text-xs rounded-lg gap-1.5',
      md: 'h-9 px-4 text-sm rounded-xl gap-2',
      lg: 'h-11 px-5 text-base rounded-xl gap-2.5',
      icon: 'h-9 w-9 p-0 rounded-xl justify-center',
    };

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn(
          'inline-flex items-center justify-center font-medium transition-all duration-150 active:scale-[0.98] disabled:opacity-50 disabled:pointer-events-none disabled:active:scale-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950',
          variants[variant],
          sizes[size],
          className
        )}
        {...props}
      >
        {isLoading ? (
          <Loader2 className="w-4 h-4 animate-spin shrink-0" />
        ) : (
          leftIcon && <span className="shrink-0">{leftIcon}</span>
        )}
        {children}
        {!isLoading && rightIcon && <span className="shrink-0">{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';
