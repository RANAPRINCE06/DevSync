import React from 'react';
import { Menu, Bell, Search, Users, User as UserIcon } from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useUnreadCount } from '@/hooks/useNotifications';
import { Link } from 'react-router-dom';

interface NavbarProps {
  onOpenMobileNav: () => void;
  onOpenCommandPalette: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenMobileNav, onOpenCommandPalette }) => {
  const { activeUser, activeTeam, users, teams, setActiveUser, setActiveTeam } = useApp();
  const { data: unreadData } = useUnreadCount(activeUser?.id);
  const unreadCount = unreadData?.unreadCount || 0;

  return (
    <header className="sticky top-0 z-20 h-14 bg-slate-950/80 backdrop-blur-md border-b border-slate-850 px-4 md:px-8 flex items-center justify-between gap-4">
      {/* Left section: mobile hamburger & search */}
      <div className="flex items-center gap-3 flex-1 max-w-md">
        <button
          onClick={onOpenMobileNav}
          className="md:hidden p-2 rounded-xl text-slate-400 hover:text-slate-200 hover:bg-slate-850"
        >
          <Menu className="w-5 h-5" />
        </button>

        <button
          onClick={onOpenCommandPalette}
          className="w-full max-w-xs hidden sm:flex items-center justify-between h-8 px-3 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-400 hover:text-slate-200 hover:border-slate-700 transition-all text-left"
        >
          <div className="flex items-center gap-2">
            <Search className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-xs text-slate-400">Search DevSync...</span>
          </div>
          <kbd className="inline-flex items-center gap-0.5 px-1.5 py-0.5 text-[10px] font-mono text-slate-400 bg-slate-800 rounded border border-slate-700">
            ⌘K
          </kbd>
        </button>
      </div>

      {/* Right section: Team switcher, User switcher, Notifications */}
      <div className="flex items-center gap-3">
        {/* Team Selector */}
        {teams.length > 0 && (
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-xs">
            <Users className="w-3.5 h-3.5 text-primary-400" />
            <select
              value={activeTeam?.id || ''}
              onChange={(e) => {
                const found = teams.find((t) => t.id === e.target.value);
                if (found) setActiveTeam(found);
              }}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer text-xs"
            >
              {teams.map((t) => (
                <option key={t.id} value={t.id} className="bg-slate-900 text-slate-200">
                  {t.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* User Switcher (For testing multi-user environments) */}
        {users.length > 0 && (
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-xs hidden lg:flex">
            <UserIcon className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={activeUser?.id || ''}
              onChange={(e) => {
                const found = users.find((u) => u.id === e.target.value);
                if (found) setActiveUser(found);
              }}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer text-xs"
            >
              {users.map((u) => (
                <option key={u.id} value={u.id} className="bg-slate-900 text-slate-200">
                  {u.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Notification Bell */}
        <Link
          to="/notifications"
          className="relative p-2 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-850 transition-colors"
        >
          <Bell className="w-4 h-4" />
          {unreadCount > 0 && (
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-primary-500 ring-2 ring-slate-950 animate-pulse" />
          )}
        </Link>

        {/* User Avatar */}
        {activeUser && (
          <div className="w-8 h-8 rounded-lg bg-primary-950 border border-primary-800/80 flex items-center justify-center text-primary-300 text-xs font-bold shadow-sm">
            {activeUser.name.charAt(0).toUpperCase()}
          </div>
        )}
      </div>
    </header>
  );
};
