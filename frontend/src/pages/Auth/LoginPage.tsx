import React, { useState } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { ArrowRight, AlertCircle, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/contexts/ToastContext';
import { Button } from '@/components/ui/Button';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  // Extract redirect url if available
  const searchParams = new URLSearchParams(location.search);
  const redirectParam = searchParams.get('redirect');
  const locationFrom = (location.state as { from?: { pathname: string } })?.from?.pathname;
  const destination = redirectParam ? decodeURIComponent(redirectParam) : (locationFrom || '/dashboard');

  const validateForm = (): boolean => {
    let isValid = true;
    setEmailError('');
    setPasswordError('');
    setErrorMessage('');

    const trimmedEmail = email.trim();
    if (!trimmedEmail) {
      setEmailError('Email is required');
      isValid = false;
    } else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(trimmedEmail)) {
        setEmailError('Please enter a valid email address');
        isValid = false;
      }
    }

    if (!password) {
      setPasswordError('Password is required');
      isValid = false;
    } else if (password.length < 6) {
      setPasswordError('Password must be at least 6 characters');
      isValid = false;
    }

    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setIsLoading(true);
      setErrorMessage('');
      await login({
        email: email.trim(),
        password,
        rememberMe,
      });
      showToast('success', 'Logged in successfully!');
      navigate(destination, { replace: true });
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Invalid email or password';
      setErrorMessage(msg);
      showToast('error', 'Authentication Failed', msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#f4f6f9] dark:bg-[#0b1120] flex flex-col justify-center items-center p-4 font-sans text-slate-800 dark:text-slate-100">
      {/* DevSync Classic Header Container */}
      <div className="w-full max-w-md bg-white dark:bg-[#111827] border border-[#cfd5dc] dark:border-slate-700 rounded-[3px] shadow-sm overflow-hidden">
        {/* Top Solid Navy Header Bar */}
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
          <span className="text-[10px] text-blue-200 font-semibold uppercase tracking-wider">Secure Login</span>
        </div>

        {/* Login Form Body */}
        <div className="p-6 space-y-4">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-slate-100">Sign In to Workspace</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Enter your registered developer credentials to access your workspace.
            </p>
          </div>

          {errorMessage && (
            <div className="p-2.5 bg-red-50 dark:bg-red-950/40 border border-red-300 dark:border-red-800 rounded-[2px] text-xs text-red-800 dark:text-red-300 flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3.5" noValidate>
            {/* Email Field */}
            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-200 block">
                Developer Email Address <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type="email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (emailError) setEmailError('');
                  }}
                  placeholder="e.g. developer@devsync.io"
                  className={`w-full px-2.5 py-1.5 text-xs bg-white dark:bg-slate-900 border ${
                    emailError ? 'border-red-500 focus:border-red-600' : 'border-[#cbd5e1] dark:border-slate-700 focus:border-blue-600'
                  } rounded-[2px] text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none`}
                  required
                />
              </div>
              {emailError && <p className="text-[11px] text-red-600 font-semibold mt-0.5">{emailError}</p>}
            </div>

            {/* Password Field */}
            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-200 block">
                Password <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (passwordError) setPasswordError('');
                  }}
                  placeholder="Enter your account password"
                  className={`w-full pl-2.5 pr-8 py-1.5 text-xs bg-white dark:bg-slate-900 border ${
                    passwordError ? 'border-red-500 focus:border-red-600' : 'border-[#cbd5e1] dark:border-slate-700 focus:border-blue-600'
                  } rounded-[2px] text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none`}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                  tabIndex={-1}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                </button>
              </div>
              {passwordError && <p className="text-[11px] text-red-600 font-semibold mt-0.5">{passwordError}</p>}
            </div>

            <div className="flex items-center justify-between text-xs pt-1">
              <label className="flex items-center gap-1.5 cursor-pointer text-slate-600 dark:text-slate-400 select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="rounded-[2px] border-slate-300 text-blue-600 focus:ring-0"
                />
                Remember my session
              </label>

              <NavLink
                to="/forgot-password"
                className="text-blue-600 dark:text-blue-400 hover:underline font-semibold"
              >
                Forgot password?
              </NavLink>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full mt-2"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
            >
              Sign In to DevSync
            </Button>
          </form>

          <div className="pt-3 border-t border-slate-200 dark:border-slate-800 text-center text-xs text-slate-500">
            Don't have an account?{' '}
            <NavLink to="/register" className="text-blue-600 dark:text-blue-400 hover:underline font-bold">
              Register here
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
