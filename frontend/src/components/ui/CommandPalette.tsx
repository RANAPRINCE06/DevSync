import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  Target,
  CheckSquare,
  Users,
  Trophy,
  Medal,
  Code2,
  TrendingUp,
  X,
  ArrowRight,
  PlusCircle,
  Settings,
  Bell,
  AlarmClock,
  BarChart3,
} from 'lucide-react';
import { useGoals } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { useApp } from '@/contexts/AppContext';

interface CommandPaletteProps {
  isOpen: boolean;
  onClose: () => void;
}

interface CommandItem {
  label: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
  category: string;
}

export const CommandPalette: React.FC<CommandPaletteProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate();
  const { activeTeam, users } = useApp();
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);

  const { data: goalsPage } = useGoals({ teamId: activeTeam?.id, active: true, size: 30 });
  const { data: tasksPage } = useTasks({ teamId: activeTeam?.id, active: true, size: 30 });

  const quickNav: CommandItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: TrendingUp, category: 'Navigation' },
    { label: 'Daily Progress & Accountability', path: '/progress', icon: TrendingUp, category: 'Navigation' },
    { label: 'Learning Goals', path: '/goals', icon: Target, category: 'Navigation' },
    { label: 'Task Management', path: '/tasks', icon: CheckSquare, category: 'Navigation' },
    { label: 'Team Workspace', path: '/team', icon: Users, category: 'Navigation' },
    { label: 'Leaderboard & Standings', path: '/leaderboard', icon: Trophy, category: 'Navigation' },
    { label: 'Achievements & Badges', path: '/achievements', icon: Medal, category: 'Navigation' },
    { label: 'Coding Profiles', path: '/coding-profiles', icon: Code2, category: 'Navigation' },
    { label: 'Productivity Analytics', path: '/analytics', icon: BarChart3, category: 'Navigation' },
    { label: 'Scheduled Reminders', path: '/reminders', icon: AlarmClock, category: 'Navigation' },
    { label: 'Notifications Feed', path: '/notifications', icon: Bell, category: 'Navigation' },
    { label: 'Settings & Preferences', path: '/settings', icon: Settings, category: 'Navigation' },
  ].filter((item) => item.label.toLowerCase().includes(query.toLowerCase()));

  const quickActions: CommandItem[] = [
    { label: 'Log Today\'s Progress', path: '/progress', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Create New Goal', path: '/goals', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Create Sprint Task', path: '/tasks', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Schedule New Reminder', path: '/reminders', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Link Coding Profile', path: '/coding-profiles', icon: PlusCircle, category: 'Quick Action' },
  ].filter((item) => item.label.toLowerCase().includes(query.toLowerCase()));

  const filteredGoals: CommandItem[] =
    goalsPage?.content
      ?.filter((g) => g.title.toLowerCase().includes(query.toLowerCase()))
      .slice(0, 4)
      .map((g) => ({
        label: g.title,
        path: '/goals',
        icon: Target,
        badge: `${g.progress}%`,
        category: 'Goals',
      })) || [];

  const filteredTasks: CommandItem[] =
    tasksPage?.content
      ?.filter((t) => t.title.toLowerCase().includes(query.toLowerCase()))
      .slice(0, 4)
      .map((t) => ({
        label: t.title,
        path: '/tasks',
        icon: CheckSquare,
        badge: t.status,
        category: 'Tasks',
      })) || [];

  const filteredUsers: CommandItem[] =
    users
      .filter(
        (u) =>
          u.name.toLowerCase().includes(query.toLowerCase()) ||
          u.email.toLowerCase().includes(query.toLowerCase())
      )
      .slice(0, 3)
      .map((u) => ({
        label: u.name,
        path: '/team',
        icon: Users,
        badge: u.email,
        category: 'Team Members',
      }));

  const allResults = [
    ...quickActions,
    ...quickNav,
    ...filteredGoals,
    ...filteredTasks,
    ...filteredUsers,
  ];

  useEffect(() => {
    setSelectedIndex(0);
  }, [query]);

  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    }
  }, [isOpen]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        onClose();
      }
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
      if (isOpen && allResults.length > 0) {
        if (e.key === 'ArrowDown') {
          e.preventDefault();
          setSelectedIndex((prev) => (prev + 1) % allResults.length);
        } else if (e.key === 'ArrowUp') {
          e.preventDefault();
          setSelectedIndex((prev) => (prev - 1 + allResults.length) % allResults.length);
        } else if (e.key === 'Enter') {
          e.preventDefault();
          const target = allResults[selectedIndex];
          if (target) {
            handleSelect(target.path);
          }
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, allResults, selectedIndex]);

  if (!isOpen) return null;

  const handleSelect = (path: string) => {
    navigate(path);
    onClose();
    setQuery('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 p-4">
      <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm transition-opacity" onClick={onClose} />
      <div className="relative w-full max-w-xl rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl z-10 overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Search Input */}
        <div className="flex items-center gap-3 px-4 py-3 border-b border-slate-800">
          <Search className="w-5 h-5 text-primary-400 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type a command, action, or search..."
            className="w-full bg-transparent text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
          <kbd className="hidden sm:inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-mono text-slate-400 bg-slate-800 rounded border border-slate-700">
            ESC
          </kbd>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-100 sm:hidden">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Results List */}
        <div className="max-h-96 overflow-y-auto p-2 space-y-1">
          {allResults.length > 0 ? (
            allResults.map((item, idx) => {
              const Icon = item.icon;
              const isSelected = idx === selectedIndex;
              return (
                <button
                  key={`${item.category}-${item.label}-${idx}`}
                  onClick={() => handleSelect(item.path)}
                  className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs transition-colors text-left ${
                    isSelected
                      ? 'bg-primary-600/20 text-slate-100 border border-primary-500/30'
                      : 'text-slate-300 hover:bg-slate-850 border border-transparent'
                  }`}
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <Icon className={`w-4 h-4 shrink-0 ${isSelected ? 'text-primary-400' : 'text-slate-400'}`} />
                    <span className="truncate">{item.label}</span>
                    <span className="text-[9px] px-1.5 py-0.2 rounded bg-slate-800 text-slate-400 uppercase tracking-wider font-mono">
                      {item.category}
                    </span>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    {item.badge && (
                      <span className="text-[10px] text-slate-400 font-mono">{item.badge}</span>
                    )}
                    <ArrowRight className={`w-3.5 h-3.5 ${isSelected ? 'opacity-100 text-primary-400' : 'opacity-0'}`} />
                  </div>
                </button>
              );
            })
          ) : (
            <div className="text-center py-6 text-xs text-slate-500">
              No matching commands or results for "{query}"
            </div>
          )}
        </div>

        {/* Footer shortcuts helper */}
        <div className="px-4 py-2 border-t border-slate-800/80 bg-slate-950/50 flex items-center justify-between text-[11px] text-slate-500">
          <span className="flex items-center gap-2">
            <span>Use <kbd className="px-1 py-0.5 rounded bg-slate-800 text-slate-400 font-mono text-[10px]">↑</kbd> <kbd className="px-1 py-0.5 rounded bg-slate-800 text-slate-400 font-mono text-[10px]">↓</kbd> to navigate</span>
            <span><kbd className="px-1 py-0.5 rounded bg-slate-800 text-slate-400 font-mono text-[10px]">↵</kbd> to select</span>
          </span>
          <span>DevSync Command Engine</span>
        </div>
      </div>
    </div>
  );
};
