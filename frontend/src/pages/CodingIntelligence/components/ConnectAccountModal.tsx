import React, { useState } from 'react';
import { AlertCircle, Globe } from 'lucide-react';
import { CodingPlatform, PlatformConfig } from '@/types/codingIntelligence';

interface ConnectAccountModalProps {
  platform: CodingPlatform;
  config: PlatformConfig;
  isOpen: boolean;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmit: (platform: CodingPlatform, username: string) => void;
}

export const ConnectAccountModal: React.FC<ConnectAccountModalProps> = ({
  platform,
  config,
  isOpen,
  isSubmitting,
  onClose,
  onSubmit,
}) => {
  const [usernameInput, setUsernameInput] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const cleanUsername = usernameInput.trim();

    if (!cleanUsername) {
      setValidationError('Please enter a valid platform handle.');
      return;
    }

    if (!config.usernameRegex.test(cleanUsername)) {
      setValidationError(
        `Invalid username format. ${config.displayName} handles must match: ${config.usernamePlaceholder}`
      );
      return;
    }

    setValidationError(null);
    onSubmit(platform, cleanUsername);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in">
      <div className="bg-white dark:bg-surface-900 rounded-2xl border border-surface-200 dark:border-surface-800 shadow-2xl max-w-md w-full p-6 space-y-5">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-surface-900 dark:text-white">
            Link {config.displayName} Profile
          </h3>
          <button
            onClick={onClose}
            className="text-surface-400 hover:text-surface-600 dark:hover:text-surface-200 text-sm font-semibold p-1"
          >
            ✕
          </button>
        </div>

        <p className="text-xs text-surface-500">
          Enter your public handle on {config.displayName}. DevSync will verify handle structure and build your verified profile link.
        </p>

        {config.communityApi && (
          <div className="p-3 rounded-xl bg-sky-500/10 border border-sky-500/20 text-xs text-sky-700 dark:text-sky-300 flex items-start gap-2">
            <Globe className="w-4 h-4 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold block">Community Data Source:</span>
              <span>{config.syncNote}</span>
            </div>
          </div>
        )}

        {!config.syncSupported && (
          <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-xs text-amber-700 dark:text-amber-300 flex items-start gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold block">Automated Sync Notice:</span>
              <span>{config.syncNote}</span>
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-surface-700 dark:text-surface-300 mb-1.5">
              Public Handle / Username
            </label>
            <input
              type="text"
              value={usernameInput}
              onChange={(e) => {
                setUsernameInput(e.target.value);
                if (validationError) setValidationError(null);
              }}
              placeholder={config.usernamePlaceholder}
              className="w-full px-3.5 py-2.5 text-sm rounded-xl border border-surface-300 dark:border-surface-700 bg-white dark:bg-surface-800 text-surface-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
              autoFocus
            />
            {validationError && (
              <p className="text-xs text-red-500 mt-1.5 flex items-center gap-1">
                <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                {validationError}
              </p>
            )}
          </div>

          {usernameInput.trim() && (
            <div className="p-3 rounded-xl bg-surface-50 dark:bg-surface-800/60 border border-surface-200 dark:border-surface-700 text-xs flex items-center justify-between gap-2">
              <span className="text-surface-500">Constructed URL:</span>
              <span className="font-mono text-primary-600 dark:text-primary-400 font-medium break-all text-right">
                {config.urlPrefix}
                {usernameInput.trim()}
              </span>
            </div>
          )}

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-surface-600 dark:text-surface-400 hover:text-surface-900 dark:hover:text-white"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-primary-600 hover:bg-primary-700 rounded-xl shadow-sm disabled:opacity-50 flex items-center gap-1.5 transition-colors"
            >
              {isSubmitting && (
                <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
              )}
              Save & Link Profile
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
