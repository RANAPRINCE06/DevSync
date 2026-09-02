import React, { useState, useEffect } from 'react';
import { Outlet, useLocation, NavLink } from 'react-router-dom';
import { Navbar } from './Navbar';
import { CommandPalette } from '@/components/ui/CommandPalette';
import { ChevronRight } from 'lucide-react';

export const AppLayout: React.FC = () => {
  const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);
  const location = useLocation();

  // Keyboard shortcut Ctrl+K / Cmd+K
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        setIsCommandPaletteOpen((prev) => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Format breadcrumb from path
  const pathSegments = location.pathname.split('/').filter(Boolean);
  const pageTitle = pathSegments.length > 0 
    ? pathSegments[0].replace('-', ' ').replace(/\b\w/g, (l) => l.toUpperCase()) 
    : 'Dashboard';

  return (
    <div className="min-h-screen flex flex-col bg-[#f4f6f9] dark:bg-[#0b1120] text-slate-800 dark:text-slate-100 font-sans">
      {/* 1. Classic Portal Top Header */}
      <Navbar onOpenCommandPalette={() => setIsCommandPaletteOpen(true)} />

      {/* 2. Breadcrumb & Title Bar */}
      <div className="bg-white dark:bg-[#111827] border-b border-[#cfd5dc] dark:border-slate-800 px-4 py-2">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-[11px] text-slate-500 dark:text-slate-400">
            <NavLink to="/dashboard" className="hover:text-blue-600 dark:hover:text-blue-400">
              DevSync Portal
            </NavLink>
            <ChevronRight className="w-3 h-3 text-slate-400" />
            <span className="font-bold text-slate-800 dark:text-slate-200">{pageTitle}</span>
          </div>
          <div className="text-[11px] text-slate-400 hidden sm:block">
            System Online • DevSync v1.0
          </div>
        </div>
      </div>

      {/* 3. Main Content Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6">
        <Outlet />
      </main>

      {/* 4. Classic Traditional Footer */}
      <footer className="bg-white dark:bg-[#111827] border-t border-[#cfd5dc] dark:border-slate-800 py-4 px-4 text-center text-xs text-slate-500 dark:text-slate-400 mt-8">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
          <div>
            <span className="font-bold text-slate-700 dark:text-slate-300">DevSync</span> • Developer Productivity & Team Accountability Platform
          </div>
          <div className="text-[11px]">
            © {new Date().getFullYear()} DevSync Inc. All rights reserved.
          </div>
        </div>
      </footer>

      {/* Command Palette */}
      <CommandPalette
        isOpen={isCommandPaletteOpen}
        onClose={() => setIsCommandPaletteOpen(false)}
      />
    </div>
  );
};
