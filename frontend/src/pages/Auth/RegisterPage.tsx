import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { ArrowRight, AlertCircle } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/contexts/ToastContext';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [timezone, setTimezone] = useState('UTC');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !email.trim()) return;

    try {
      setIsLoading(true);
      setErrorMessage('');
      await register({
        name: name.trim(),
        email: email.trim(),
        timezone,
      });
      showToast('success', 'Account registered successfully! Welcome to DevSync.');
      navigate('/dashboard');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Registration failed. Email may already be registered.';
      setErrorMessage(msg);
      showToast('error', 'Registration Error', msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f4f6f9] dark:bg-[#0b1120] flex flex-col justify-center items-center p-4 font-sans text-slate-800 dark:text-slate-100">
      <div className="w-full max-w-md bg-white dark:bg-[#111827] border border-[#cfd5dc] dark:border-slate-700 rounded-[3px] shadow-sm overflow-hidden">
        {/* Header Bar */}
        <div className="bg-[#1e3a8a] text-white px-4 py-3 flex items-center justify-between border-b border-[#1e40af]">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-[2px] bg-white text-[#1e3a8a] flex items-center justify-center font-bold text-xs">
              DS
            </div>
            <div>
              <span className="font-extrabold text-sm block leading-tight">DEVSYNC</span>
              <span className="text-[9px] uppercase tracking-widest text-blue-200 block">Developer Portal</span>
            </div>
          </div>
          <span className="text-[10px] text-blue-200 font-semibold uppercase">New Account</span>
        </div>

        {/* Form Body */}
        <div className="p-6 space-y-4">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-slate-100">Create Developer Account</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Join DevSync to synchronize daily accountability with your engineering team.
            </p>
          </div>

          {errorMessage && (
            <div className="p-2.5 bg-red-50 dark:bg-red-950/40 border border-red-300 dark:border-red-800 rounded-[2px] text-xs text-red-800 dark:text-red-300 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3.5">
            <Input
              label="Full Name *"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Alex Mercer"
              required
            />

            <Input
              label="Developer Email Address *"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. alex@devsync.io"
              required
            />

            <Input
              label="Timezone *"
              value={timezone}
              onChange={(e) => setTimezone(e.target.value)}
              placeholder="e.g. UTC, Asia/Kolkata"
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
            >
              Complete Registration
            </Button>
          </form>

          <div className="pt-3 border-t border-slate-200 dark:border-slate-800 text-center text-xs text-slate-500">
            Already have an account?{' '}
            <NavLink to="/login" className="text-blue-600 dark:text-blue-400 hover:underline font-bold">
              Sign In
            </NavLink>
          </div>
        </div>
      </div>

      <div className="mt-4 text-[11px] text-slate-400 text-center">
        DevSync Platform • System Operational
      </div>
    </div>
  );
};
