import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  Target,
  CheckSquare,
  Users,
  Trophy,
  Award,
  Code2,
  TrendingUp,
  X,
  ArrowRight,
  PlusCircle,
  Settings,
  Bell,
  Clock,
  BarChart3,
} from 'lucide-react';
import { useGoals } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { useApp } from '@/contexts/AppContext';
import { filterRealUsers } from '@/lib/userFilter';

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

  const { data: goalsPage } = useGoals({ teamId: activeTeam?.id, active: true, size: 50 });
  const { data: tasksPage } = useTasks({ teamId: activeTeam?.id, active: true, size: 50 });

  const quickNav: CommandItem[] = [
    { label: 'Dashboard', path: '/dashboard', icon: TrendingUp, category: 'Navigation' },
    { label: 'Daily Progress Logs', path: '/progress', icon: Clock, category: 'Navigation' },
    { label: 'Learning Goals', path: '/goals', icon: Target, category: 'Navigation' },
    { label: 'Task Management', path: '/tasks', icon: CheckSquare, category: 'Navigation' },
    { label: 'Team Workspace', path: '/team', icon: Users, category: 'Navigation' },
    { label: 'Leaderboard & Standings', path: '/leaderboard', icon: Trophy, category: 'Navigation' },
    { label: 'Achievements Catalog', path: '/achievements', icon: Award, category: 'Navigation' },
    { label: 'Coding Profiles', path: '/coding-profiles', icon: Code2, category: 'Navigation' },
    { label: 'Productivity Analytics', path: '/analytics', icon: BarChart3, category: 'Navigation' },
    { label: 'Scheduled Reminders', path: '/reminders', icon: Clock, category: 'Navigation' },
    { label: 'Notifications Feed', path: '/notifications', icon: Bell, category: 'Navigation' },
    { label: 'Settings & Preferences', path: '/settings', icon: Settings, category: 'Navigation' },
  ].filter((item) => item.label.toLowerCase().includes(query.toLowerCase()));

  const quickActions: CommandItem[] = [
    { label: 'Log Today\'s Progress', path: '/progress', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Create New Goal', path: '/goals', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Create Sprint Task', path: '/tasks', icon: PlusCircle, category: 'Quick Action' },
    { label: 'Schedule Reminder', path: '/reminders', icon: PlusCircle, category: 'Quick Action' },
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

  const realUsers = filterRealUsers(users);
  const filteredUsers: CommandItem[] = realUsers
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
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 p-4 bg-black/50 backdrop-blur-xs">
      <div className="relative w-full max-w-xl bg-white dark:bg-[#111827] border border-[#cfd5dc] dark:border-slate-700 rounded-[3px] shadow-2xl z-10 overflow-hidden">
        {/* Search Input Bar */}
        <div className="flex items-center gap-2.5 px-3.5 py-2.5 bg-[#f8fafc] dark:bg-slate-800 border-b border-[#cfd5dc] dark:border-slate-700">
          <Search className="w-4 h-4 text-blue-600 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type a command, action, or module name..."
            className="w-full bg-transparent text-xs text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none"
          />
          <kbd className="text-[10px] font-mono text-slate-500 bg-white dark:bg-slate-900 px-1.5 py-0.5 rounded border border-slate-300 dark:border-slate-600">
            ESC
          </kbd>
          <button onClick={onClose} className="p-0.5 text-slate-400 hover:text-slate-700">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Results List */}
        <div className="max-h-80 overflow-y-auto p-1.5 space-y-0.5">
          {allResults.length > 0 ? (
            allResults.map((item, idx) => {
              const Icon = item.icon;
              const isSelected = idx === selectedIndex;
              return (
                <button
                  key={`${item.category}-${item.label}-${idx}`}
                  onClick={() => handleSelect(item.path)}
                  className={`w-full flex items-center justify-between px-3 py-2 rounded-[2px] text-xs transition-colors text-left ${
                    isSelected
                      ? 'bg-blue-600 text-white font-semibold'
                      : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                >
                  <div className="flex items-center gap-2 min-w-0">
                    <Icon className={`w-3.5 h-3.5 shrink-0 ${isSelected ? 'text-white' : 'text-blue-600'}`} />
                    <span className="truncate">{item.label}</span>
                    <span
                      className={`text-[9px] px-1 py-0.2 rounded font-mono uppercase ${
                        isSelected
                          ? 'bg-blue-800 text-blue-100'
                          : 'bg-slate-100 dark:bg-slate-800 text-slate-500'
                      }`}
                    >
                      {item.category}
                    </span>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    {item.badge && (
                      <span className={`text-[10px] font-mono ${isSelected ? 'text-blue-100' : 'text-slate-400'}`}>
                        {item.badge}
                      </span>
                    )}
                    <ArrowRight className={`w-3 h-3 ${isSelected ? 'opacity-100' : 'opacity-0'}`} />
                  </div>
                </button>
              );
            })
          ) : (
            <div className="text-center py-6 text-xs text-slate-500">
              No matching commands or items found for "{query}"
            </div>
          )}
        </div>

        {/* Footer shortcuts */}
        <div className="px-3 py-1.5 bg-[#f8fafc] dark:bg-slate-800 border-t border-[#cfd5dc] dark:border-slate-700 flex items-center justify-between text-[10px] text-slate-500">
          <span>Use ↑ ↓ to navigate • ↵ to select</span>
          <span>DevSync Portal Engine</span>
        </div>
      </div>
    </div>
  );
};
