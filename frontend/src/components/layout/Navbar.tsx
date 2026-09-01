import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Bell,
  Search,
  Menu,
  ChevronDown,
  LogOut,
  Settings,
  CheckCheck,
  Calendar,
} from 'lucide-react';
import { useApp } from '@/contexts/AppContext';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/contexts/ToastContext';
import {
  useUnreadCount,
  useUnreadNotifications,
  useMarkAllAsRead,
} from '@/hooks/useNotifications';
import { formatDate } from '@/lib/utils';

interface NavbarProps {
  onOpenMobileNav: () => void;
  onOpenCommandPalette: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenMobileNav, onOpenCommandPalette }) => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { activeTeam, teams, setActiveTeam } = useApp();
  const { showToast } = useToast();

  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);

  const userMenuRef = useRef<HTMLDivElement>(null);
  const notifMenuRef = useRef<HTMLDivElement>(null);

  const { data: unreadData } = useUnreadCount(user?.id);
  const { data: unreadNotificationsPage } = useUnreadNotifications({ size: 5 });
  const markAllMutation = useMarkAllAsRead();

  const unreadCount = unreadData?.unreadCount || 0;
  const unreadList = unreadNotificationsPage?.content || [];

  // Close menus on outside click
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(e.target as Node)) {
        setIsUserMenuOpen(false);
      }
      if (notifMenuRef.current && !notifMenuRef.current.contains(e.target as Node)) {
        setIsNotificationsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    await logout();
    showToast('info', 'Logged out successfully');
    navigate('/login', { replace: true });
  };

  const handleMarkAllRead = async () => {
    if (!user) return;
    try {
      await markAllMutation.mutateAsync(user.id);
      showToast('success', 'All notifications marked as read');
    } catch {
      showToast('error', 'Failed to mark all as read');
    }
  };

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

      {/* Right section: Team switcher, Notifications, User Menu */}
      <div className="flex items-center gap-3">
        {/* Team switcher dropdown */}
        {teams.length > 0 && (
          <div className="relative">
            <select
              value={activeTeam?.id || ''}
              onChange={(e) => {
                const found = teams.find((t) => t.id === e.target.value);
                if (found) setActiveTeam(found);
              }}
              className="h-8 pl-2.5 pr-8 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-200 focus:outline-none focus:ring-1 focus:ring-primary-500/50 appearance-none cursor-pointer"
            >
              {teams.map((team) => (
                <option key={team.id} value={team.id}>
                  {team.name}
                </option>
              ))}
            </select>
            <ChevronDown className="w-3 h-3 text-slate-500 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          </div>
        )}

        {/* Notifications Bell with Popover */}
        <div className="relative" ref={notifMenuRef}>
          <button
            onClick={() => setIsNotificationsOpen((prev) => !prev)}
            className="relative p-2 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent hover:border-slate-800 transition-all"
            title="Notifications"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 w-2 h-2 rounded-full bg-primary-500 ring-2 ring-slate-950 animate-pulse" />
            )}
          </button>

          {/* Popover */}
          {isNotificationsOpen && (
            <div className="absolute right-0 mt-2 w-80 rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl p-3 z-30 animate-in zoom-in-95 duration-150 space-y-2">
              <div className="flex items-center justify-between pb-2 border-b border-slate-800">
                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-bold text-slate-100">Notifications</span>
                  {unreadCount > 0 && (
                    <span className="px-1.5 py-0.2 rounded-full bg-primary-500/20 text-primary-300 text-[10px] font-bold border border-primary-500/30">
                      {unreadCount} new
                    </span>
                  )}
                </div>
                {unreadCount > 0 && (
                  <button
                    onClick={handleMarkAllRead}
                    disabled={markAllMutation.isPending}
                    className="text-[11px] text-primary-400 hover:text-primary-300 flex items-center gap-1 font-medium transition-colors"
                  >
                    <CheckCheck className="w-3 h-3" /> Mark all read
                  </button>
                )}
              </div>

              {/* Notification items list */}
              <div className="space-y-1 max-h-64 overflow-y-auto">
                {unreadList.length > 0 ? (
                  unreadList.map((n) => (
                    <div
                      key={n.id}
                      className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800/80 hover:border-slate-700/80 transition-all space-y-0.5"
                    >
                      <p className="text-xs font-semibold text-slate-100 truncate">{n.title}</p>
                      <p className="text-[11px] text-slate-400 line-clamp-2">{n.message}</p>
                      <span className="text-[9px] text-slate-500 block pt-0.5 flex items-center gap-1">
                        <Calendar className="w-2.5 h-2.5" />
                        {formatDate(n.createdAt)}
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-6 text-xs text-slate-500">
                    No unread notifications!
                  </div>
                )}
              </div>

              {/* Footer link */}
              <div className="pt-2 border-t border-slate-800 text-center">
                <Link
                  to="/notifications"
                  onClick={() => setIsNotificationsOpen(false)}
                  className="text-xs font-medium text-primary-400 hover:text-primary-300 transition-colors"
                >
                  View all notifications →
                </Link>
              </div>
            </div>
          )}
        </div>

        {/* User Session Dropdown */}
        <div className="relative" ref={userMenuRef}>
          <button
            onClick={() => setIsUserMenuOpen((prev) => !prev)}
            className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-slate-900 border border-transparent hover:border-slate-800 transition-all"
          >
            <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-primary-600 to-indigo-500 flex items-center justify-center text-white font-bold text-xs shadow-sm shrink-0">
              {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="hidden lg:flex flex-col text-left">
              <span className="text-xs font-semibold text-slate-200 leading-tight">
                {user?.name || 'Developer'}
              </span>
              <span className="text-[10px] text-slate-500 leading-tight truncate max-w-[120px]">
                {user?.email || 'dev@devsync.io'}
              </span>
            </div>
            <ChevronDown className="w-3 h-3 text-slate-500" />
          </button>

          {/* User Menu Popover */}
          {isUserMenuOpen && (
            <div className="absolute right-0 mt-2 w-48 rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl p-1.5 z-30 animate-in zoom-in-95 duration-150 space-y-1">
              <div className="px-3 py-2 border-b border-slate-800">
                <p className="text-xs font-bold text-slate-100 truncate">{user?.name}</p>
                <p className="text-[10px] text-slate-500 truncate">{user?.email}</p>
              </div>

              <Link
                to="/settings"
                onClick={() => setIsUserMenuOpen(false)}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-300 hover:text-slate-100 hover:bg-slate-800 transition-colors"
              >
                <Settings className="w-3.5 h-3.5 text-slate-400" />
                <span>Settings</span>
              </Link>

              <button
                onClick={handleLogout}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-rose-400 hover:text-rose-300 hover:bg-rose-950/30 transition-colors text-left"
              >
                <LogOut className="w-3.5 h-3.5 text-rose-400" />
                <span>Log Out</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
