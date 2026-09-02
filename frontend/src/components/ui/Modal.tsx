import React, { useEffect, ReactNode } from 'react';
import { X } from 'lucide-react';

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children: ReactNode;
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl';
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  description,
  children,
  maxWidth = 'md',
}) => {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const maxWidths = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/50 backdrop-blur-xs">
      <div
        className={`w-full ${maxWidths[maxWidth]} bg-white dark:bg-[#111827] border border-[#cfd5dc] dark:border-slate-700 rounded-[3px] shadow-lg overflow-hidden animate-in fade-in zoom-in-95 duration-150`}
        role="dialog"
        aria-modal="true"
      >
        {/* Classic Modal Header */}
        <div className="bg-[#1e3a8a] text-white px-3.5 py-2.5 flex items-center justify-between border-b border-[#1e40af]">
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider">{title}</h3>
            {description && <p className="text-[11px] text-blue-200 mt-0.5">{description}</p>}
          </div>
          <button
            onClick={onClose}
            className="p-1 text-blue-200 hover:text-white hover:bg-blue-800 rounded-[2px] transition-colors"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
};
