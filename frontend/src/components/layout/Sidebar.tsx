import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  TrendingUp,
  Target,
  CheckSquare,
  Users,
  Trophy,
  Medal,
  Code2,
  Bell,
  Settings,
  ChevronLeft,
  ChevronRight,
  CodeXml,
  X,
  LogOut,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/contexts/ToastContext';
import { useUnreadCount } from '@/hooks/useNotifications';

interface SidebarProps {
  isCollapsed: boolean;
  setIsCollapsed: (collapsed: boolean) => void;
  isMobileOpen: boolean;
  setIsMobileOpen: (open: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  isCollapsed,
  setIsCollapsed,
  isMobileOpen,
  setIsMobileOpen,
}) => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const { data: unreadData } = useUnreadCount(user?.id);
  const unreadCount = unreadData?.unreadCount || 0;

  const handleLogout = async () => {
    await logout();
    showToast('info', 'Logged out successfully');
    navigate('/login', { replace: true });
  };

  const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/progress', label: 'My Progress', icon: TrendingUp },
    { to: '/goals', label: 'Goals', icon: Target },
    { to: '/tasks', label: 'Tasks', icon: CheckSquare },
    { to: '/team', label: 'Team', icon: Users },
    { to: '/leaderboard', label: 'Leaderboard', icon: Trophy },
    { to: '/achievements', label: 'Achievements', icon: Medal },
    { to: '/coding-profiles', label: 'Coding Profiles', icon: Code2 },
    {
      to: '/notifications',
      label: 'Notifications',
      icon: Bell,
      badge: unreadCount > 0 ? unreadCount : undefined,
    },
  ];

  const bottomItems = [
    { to: '/settings', label: 'Settings', icon: Settings },
  ];

  const renderNavList = () => (
    <div className="flex flex-col justify-between h-full">
      {/* Top section: Logo & Nav items */}
      <div className="space-y-6">
        <div className="flex items-center justify-between px-3 pt-2">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-primary-600 to-indigo-500 flex items-center justify-center text-white shadow-md shadow-primary-950/50">
              <CodeXml className="w-5 h-5" />
            </div>
            {(!isCollapsed || isMobileOpen) && (
              <div>
                <span className="font-bold text-base tracking-tight text-white flex items-center gap-1.5">
                  Dev<span className="text-primary-400">Sync</span>
                </span>
                <span className="block text-[10px] text-slate-400 font-mono">v0.1.0-alpha</span>
              </div>
            )}
          </div>
          {/* Mobile close button */}
          <button
            onClick={() => setIsMobileOpen(false)}
            className="md:hidden text-slate-400 hover:text-slate-100 p-1.5 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Nav Links */}
        <nav className="space-y-1 px-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setIsMobileOpen(false)}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all duration-150 group relative',
                    isActive
                      ? 'bg-primary-600/15 text-primary-300 font-semibold border border-primary-500/20'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-850'
                  )
                }
              >
                <Icon className="w-4 h-4 shrink-0 transition-colors" />
                {(!isCollapsed || isMobileOpen) && (
                  <span className="flex-1 truncate">{item.label}</span>
                )}
                {(!isCollapsed || isMobileOpen) && item.badge !== undefined && (
                  <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-primary-500 text-white font-bold">
                    {item.badge}
                  </span>
                )}
                {isCollapsed && !isMobileOpen && item.badge !== undefined && (
                  <span className="absolute top-2 right-2 w-2 h-2 rounded-full bg-primary-500" />
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Bottom section: Settings & Profile */}
      <div className="space-y-3 px-2 pb-3">
        <nav className="space-y-1">
          {bottomItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setIsMobileOpen(false)}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium transition-all duration-150',
                    isActive
                      ? 'bg-primary-600/15 text-primary-300 font-semibold border border-primary-500/20'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-850'
                  )
                }
              >
                <Icon className="w-4 h-4 shrink-0" />
                {(!isCollapsed || isMobileOpen) && <span>{item.label}</span>}
              </NavLink>
            );
          })}
        </nav>

        {/* User Card & Logout */}
        {user && (
          <div className="p-2 rounded-xl bg-slate-900/90 border border-slate-800/80 flex items-center justify-between gap-2">
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-primary-600 to-indigo-500 flex items-center justify-center text-white text-xs font-bold shrink-0">
                {user.name.charAt(0).toUpperCase()}
              </div>
              {(!isCollapsed || isMobileOpen) && (
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-medium text-slate-200 truncate">{user.name}</p>
                  <p className="text-[10px] text-slate-400 truncate">{user.email}</p>
                </div>
              )}
            </div>

            {(!isCollapsed || isMobileOpen) && (
              <button
                onClick={handleLogout}
                className="p-1 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-rose-950/20 transition-colors"
                title="Log out"
              >
                <LogOut className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        )}

        {/* Desktop collapse toggle */}
        <button
          onClick={() => setIsCollapsed(!isCollapsed)}
          className="hidden md:flex items-center justify-center w-full py-1.5 rounded-lg text-slate-500 hover:text-slate-300 hover:bg-slate-850 text-xs transition-colors"
        >
          {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop Sidebar */}
      <aside
        className={cn(
          'hidden md:block fixed top-0 left-0 h-screen z-30 bg-slate-950 border-r border-slate-850 transition-all duration-200 p-2',
          isCollapsed ? 'w-16' : 'w-60'
        )}
      >
        {renderNavList()}
      </aside>

      {/* Mobile Drawer */}
      {isMobileOpen && (
        <div className="fixed inset-0 z-50 md:hidden flex">
          <div
            className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm transition-opacity"
            onClick={() => setIsMobileOpen(false)}
          />
          <div className="relative w-64 bg-slate-950 border-r border-slate-800 h-full p-3 shadow-2xl z-10 animate-in slide-in-from-left duration-200">
            {renderNavList()}
          </div>
        </div>
      )}
    </>
  );
};
