import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  Code2,
  Search,
  Bell,
  Sun,
  Moon,
  LogOut,
  Users,
  ChevronDown,
  LayoutDashboard,
  Calendar,
  Target,
  CheckSquare,
  Trophy,
  Award,
  BarChart3,
  Clock,
  Settings,
  PlusCircle,
} from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useApp } from '@/contexts/AppContext';
import { useTheme } from '@/contexts/ThemeContext';
import { useUnreadCount } from '@/hooks/useNotifications';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { useToast } from '@/contexts/ToastContext';

interface NavbarProps {
  onOpenCommandPalette: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenCommandPalette }) => {
  const { user, logout } = useAuth();
  const { activeTeam, teams, setActiveTeam, createTeam } = useApp();
  const { theme, setTheme } = useTheme();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const { data: unreadData } = useUnreadCount(user?.id);
  const unreadCount = unreadData?.unreadCount || 0;

  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isTeamMenuOpen, setIsTeamMenuOpen] = useState(false);
  const [isCreateTeamModalOpen, setIsCreateTeamModalOpen] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');
  const [newTeamDesc, setNewTeamDesc] = useState('');
  const [isCreatingTeam, setIsCreatingTeam] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleCreateTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTeamName.trim()) return;
    try {
      setIsCreatingTeam(true);
      await createTeam(newTeamName.trim(), newTeamDesc.trim() || undefined);
      showToast('success', 'Team created successfully! You are now the Team Lead.');
      setIsCreateTeamModalOpen(false);
      setNewTeamName('');
      setNewTeamDesc('');
    } catch (err: unknown) {
      showToast('error', 'Failed to create team', err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setIsCreatingTeam(false);
    }
  };

  const navLinks = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/progress', label: 'Daily Progress', icon: Calendar },
    { to: '/goals', label: 'Goals', icon: Target },
    { to: '/tasks', label: 'Tasks', icon: CheckSquare },
    { to: '/team', label: 'Team', icon: Users },
    { to: '/leaderboard', label: 'Leaderboard', icon: Trophy },
    { to: '/achievements', label: 'Achievements', icon: Award },
    { to: '/coding-profiles', label: 'Profiles', icon: Code2 },
    { to: '/analytics', label: 'Analytics', icon: BarChart3 },
    { to: '/reminders', label: 'Reminders', icon: Clock },
    { to: '/notifications', label: 'Alerts', icon: Bell },
    { to: '/settings', label: 'Settings', icon: Settings },
  ];

  return (
    <header className="border-b border-[#1e3a8a] bg-[#1e3a8a] text-white shadow-xs sticky top-0 z-40">
      {/* 1. Main Top Header Bar */}
      <div className="max-w-7xl mx-auto px-4 h-12 flex items-center justify-between gap-4">
        {/* Logo & Portal Identity */}
        <div className="flex items-center gap-3">
          <NavLink to="/dashboard" className="flex items-center gap-2 text-white font-bold text-sm tracking-wide">
            <div className="w-7 h-7 rounded-[2px] bg-white text-[#1e3a8a] flex items-center justify-center font-black text-xs shadow-xs">
              DS
            </div>
            <div className="leading-tight">
              <span className="font-extrabold text-sm block">DEVSYNC</span>
              <span className="text-[9px] uppercase tracking-widest text-blue-200 block">Developer Portal</span>
            </div>
          </NavLink>

          {/* Team Selector Dropdown */}
          <div className="relative hidden md:block">
            <button
              onClick={() => setIsTeamMenuOpen(!isTeamMenuOpen)}
              className="flex items-center gap-1.5 px-2.5 py-1 bg-[#1e40af] hover:bg-[#1d4ed8] border border-blue-400/40 rounded-[2px] text-xs font-medium text-white transition-colors"
            >
              <Users className="w-3.5 h-3.5 text-blue-200" />
              <span className="max-w-[140px] truncate">{activeTeam?.name || 'Select Team'}</span>
              <ChevronDown className="w-3 h-3 text-blue-200" />
            </button>

            {isTeamMenuOpen && (
              <div className="absolute left-0 mt-1 w-56 bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-700 rounded-[2px] shadow-lg py-1 z-50 text-slate-800 dark:text-slate-200 text-xs">
                <div className="px-3 py-1 font-bold text-[10px] uppercase text-slate-400 border-b border-slate-200 dark:border-slate-800">
                  Switch Team Workspace
                </div>
                <div className="max-h-48 overflow-y-auto">
                  {teams.map((t) => (
                    <button
                      key={t.id}
                      onClick={() => {
                        setActiveTeam(t);
                        setIsTeamMenuOpen(false);
                      }}
                      className={`w-full text-left px-3 py-1.5 hover:bg-blue-50 dark:hover:bg-slate-800 flex items-center justify-between ${
                        activeTeam?.id === t.id ? 'font-bold text-blue-700 dark:text-blue-400 bg-blue-50/50' : ''
                      }`}
                    >
                      <span className="truncate">{t.name}</span>
                      {activeTeam?.id === t.id && <span className="text-[10px] text-blue-600">Active</span>}
                    </button>
                  ))}
                </div>
                <div className="border-t border-slate-200 dark:border-slate-800 p-1.5">
                  <button
                    onClick={() => {
                      setIsTeamMenuOpen(false);
                      setIsCreateTeamModalOpen(true);
                    }}
                    className="w-full flex items-center justify-center gap-1.5 py-1 text-xs bg-[#f1f5f9] hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 font-semibold rounded-[2px]"
                  >
                    <PlusCircle className="w-3.5 h-3.5 text-blue-600" />
                    Create New Team
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Section: Search, Notifications, Theme, User */}
        <div className="flex items-center gap-2">
          {/* Quick Search */}
          <button
            onClick={onOpenCommandPalette}
            className="flex items-center gap-2 px-2.5 py-1 bg-[#1e40af] hover:bg-[#1d4ed8] border border-blue-400/40 rounded-[2px] text-xs text-blue-100 transition-colors"
            title="Search or press Ctrl+K"
          >
            <Search className="w-3.5 h-3.5" />
            <span className="hidden sm:inline text-[11px]">Quick Search</span>
            <kbd className="hidden sm:inline bg-[#1e3a8a] text-[10px] px-1 rounded border border-blue-400/50 font-mono">
              Ctrl+K
            </kbd>
          </button>

          {/* Notifications */}
          <NavLink
            to="/notifications"
            className="relative p-1.5 bg-[#1e40af] hover:bg-[#1d4ed8] rounded-[2px] text-white border border-blue-400/40 transition-colors"
            title="Notifications"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-600 text-white text-[9px] font-bold px-1 rounded-full border border-white">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </NavLink>

          {/* Theme Toggle */}
          <button
            onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            className="p-1.5 bg-[#1e40af] hover:bg-[#1d4ed8] rounded-[2px] text-white border border-blue-400/40 transition-colors"
            title="Toggle theme"
          >
            {theme === 'dark' ? <Sun className="w-4 h-4 text-amber-300" /> : <Moon className="w-4 h-4 text-blue-200" />}
          </button>

          {/* User Profile Menu */}
          <div className="relative">
            <button
              onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
              className="flex items-center gap-1.5 px-2 py-1 bg-[#1e40af] hover:bg-[#1d4ed8] border border-blue-400/40 rounded-[2px] text-xs text-white transition-colors"
            >
              <div className="w-5 h-5 rounded-[2px] bg-white text-[#1e3a8a] flex items-center justify-center font-bold text-[10px]">
                {user?.name?.charAt(0).toUpperCase() || 'U'}
              </div>
              <span className="hidden md:inline font-semibold max-w-[100px] truncate">{user?.name || 'User'}</span>
              <ChevronDown className="w-3 h-3 text-blue-200" />
            </button>

            {isUserMenuOpen && (
              <div className="absolute right-0 mt-1 w-52 bg-white dark:bg-slate-900 border border-[#cfd5dc] dark:border-slate-700 rounded-[2px] shadow-lg py-1 z-50 text-slate-800 dark:text-slate-200 text-xs">
                <div className="px-3 py-2 border-b border-slate-200 dark:border-slate-800">
                  <div className="font-bold text-slate-900 dark:text-slate-100">{user?.name}</div>
                  <div className="text-[11px] text-slate-500 truncate">{user?.email}</div>
                </div>
                <NavLink
                  to="/settings"
                  onClick={() => setIsUserMenuOpen(false)}
                  className="w-full text-left px-3 py-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 flex items-center gap-2"
                >
                  <Settings className="w-3.5 h-3.5 text-slate-500" />
                  Account Settings
                </NavLink>
                <div className="border-t border-slate-200 dark:border-slate-800 my-1" />
                <button
                  handle-event="logout"
                  onClick={handleLogout}
                  className="w-full text-left px-3 py-1.5 hover:bg-red-50 dark:hover:bg-red-950/40 text-red-600 flex items-center gap-2 font-medium"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  Sign Out
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 2. Classic Horizontal Navigation Menu Bar */}
      <nav className="bg-[#1e40af] border-t border-blue-600/40 px-4 overflow-x-auto">
        <div className="max-w-7xl mx-auto flex items-center gap-1 text-xs">
          {navLinks.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `px-3 py-2 flex items-center gap-1.5 whitespace-nowrap font-medium transition-colors border-b-2 ${
                  isActive
                    ? 'bg-white/10 text-white border-white font-bold'
                    : 'text-blue-100 hover:text-white hover:bg-white/5 border-transparent'
                }`
              }
            >
              <item.icon className="w-3.5 h-3.5 opacity-80" />
              {item.label}
            </NavLink>
          ))}
        </div>
      </nav>

      {/* Create Team Modal */}
      <Modal
        isOpen={isCreateTeamModalOpen}
        onClose={() => setIsCreateTeamModalOpen(false)}
        title="Create Team Workspace"
        description="Start a new team accountability workspace. You will automatically become the Team Lead."
      >
        <form onSubmit={handleCreateTeam} className="space-y-3">
          <Input
            label="Team Name *"
            value={newTeamName}
            onChange={(e) => setNewTeamName(e.target.value)}
            placeholder="e.g. Core Engineering Team"
            required
          />
          <Input
            label="Description (Optional)"
            value={newTeamDesc}
            onChange={(e) => setNewTeamDesc(e.target.value)}
            placeholder="e.g. Daily sprint tracking & backend mastery"
          />

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-200 dark:border-slate-700">
            <Button type="button" variant="outline" size="sm" onClick={() => setIsCreateTeamModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm" isLoading={isCreatingTeam}>
              Create Team & Become Lead
            </Button>
          </div>
        </form>
      </Modal>
    </header>
  );
};
