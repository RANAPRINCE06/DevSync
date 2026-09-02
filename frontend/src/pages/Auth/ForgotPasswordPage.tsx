import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { ArrowLeft, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';

export const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;
    setIsSubmitted(true);
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
          <span className="text-[10px] text-blue-200 font-semibold uppercase">Account Recovery</span>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4">
          {!isSubmitted ? (
            <>
              <div>
                <h2 className="text-base font-bold text-slate-900 dark:text-slate-100">Reset Account Access</h2>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                  Enter your registered developer email address to receive access verification instructions.
                </p>
              </div>

              <form onSubmit={handleSubmit} className="space-y-3.5">
                <Input
                  label="Registered Email Address *"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="e.g. alex@devsync.io"
                  required
                />

                <Button type="submit" variant="primary" size="md" className="w-full">
                  Send Recovery Link
                </Button>
              </form>
            </>
          ) : (
            <div className="p-4 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 rounded-[2px] text-center space-y-2">
              <CheckCircle2 className="w-6 h-6 text-emerald-600 mx-auto" />
              <h3 className="font-bold text-sm text-emerald-900 dark:text-emerald-200">Recovery Email Dispatched</h3>
              <p className="text-xs text-emerald-800 dark:text-emerald-300">
                If an account exists for <span className="font-bold">{email}</span>, account restoration instructions have been sent.
              </p>
            </div>
          )}

          <div className="pt-3 border-t border-slate-200 dark:border-slate-800 text-center text-xs">
            <NavLink
              to="/login"
              className="inline-flex items-center gap-1.5 text-blue-600 dark:text-blue-400 hover:underline font-bold"
            >
              <ArrowLeft className="w-3.5 h-3.5" /> Back to Sign In
            </NavLink>
          </div>
        </div>
      </div>
    </div>
  );
};
