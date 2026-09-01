import React, { useState, useEffect } from 'react';
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
} from 'lucide-react';
import { useGoals } from '@/hooks/useGoals';
import { useTasks } from '@/hooks/useTasks';
import { useApp } from '@/contexts/AppContext';

interface CommandPaletteProps {
  isOpen: boolean;
  onClose: () => void;
}

export const CommandPalette: React.FC<CommandPaletteProps> = ({ isOpen, onClose }) => {
  const navigate = useNavigate();
  const { activeTeam, users } = useApp();
  const [query, setQuery] = useState('');

  const { data: goalsPage } = useGoals({ teamId: activeTeam?.id, active: true, size: 50 });
  const { data: tasksPage } = useTasks({ teamId: activeTeam?.id, active: true, size: 50 });

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        if (isOpen) {
          onClose();
        } else {
          // Open handled externally or if trigger exists
        }
      }
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const quickNav = [
    { label: 'Dashboard', path: '/dashboard', icon: TrendingUp },
    { label: 'My Progress', path: '/progress', icon: TrendingUp },
    { label: 'Goals', path: '/goals', icon: Target },
    { label: 'Tasks', path: '/tasks', icon: CheckSquare },
    { label: 'Team', path: '/team', icon: Users },
    { label: 'Leaderboard', path: '/leaderboard', icon: Trophy },
    { label: 'Achievements', path: '/achievements', icon: Medal },
    { label: 'Coding Profiles', path: '/coding-profiles', icon: Code2 },
  ].filter((item) => item.label.toLowerCase().includes(query.toLowerCase()));

  const filteredGoals =
    goalsPage?.content?.filter((g) => g.title.toLowerCase().includes(query.toLowerCase())).slice(0, 4) || [];

  const filteredTasks =
    tasksPage?.content?.filter((t) => t.title.toLowerCase().includes(query.toLowerCase())).slice(0, 4) || [];

  const filteredUsers =
    users.filter((u) => u.name.toLowerCase().includes(query.toLowerCase()) || u.email.toLowerCase().includes(query.toLowerCase())).slice(0, 3);

  const handleSelect = (path: string) => {
    navigate(path);
    onClose();
    setQuery('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 p-4">
      <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm transition-opacity" onClick={onClose} />
      <div className="relative w-full max-w-xl rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl z-10 overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Search input header */}
        <div className="flex items-center gap-3 px-4 py-3 border-b border-slate-800">
          <Search className="w-5 h-5 text-primary-400 shrink-0" />
          <input
            type="text"
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type a command or search goals, tasks, members..."
            className="w-full bg-transparent text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none"
          />
          <kbd className="hidden sm:inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-mono text-slate-400 bg-slate-800 rounded border border-slate-700">
            ESC
          </kbd>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-100 sm:hidden">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Results Body */}
        <div className="max-h-96 overflow-y-auto p-2 space-y-4">
          {/* Quick Navigation */}
          {quickNav.length > 0 && (
            <div>
              <p className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Navigation</p>
              <div className="space-y-0.5">
                {quickNav.map((item) => {
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.path}
                      onClick={() => handleSelect(item.path)}
                      className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs text-slate-300 hover:text-slate-100 hover:bg-slate-800/70 transition-colors group text-left"
                    >
                      <div className="flex items-center gap-2.5">
                        <Icon className="w-4 h-4 text-slate-400 group-hover:text-primary-400" />
                        <span>{item.label}</span>
                      </div>
                      <ArrowRight className="w-3.5 h-3.5 text-slate-500 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Goals */}
          {filteredGoals.length > 0 && (
            <div>
              <p className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Goals</p>
              <div className="space-y-0.5">
                {filteredGoals.map((goal) => (
                  <button
                    key={goal.id}
                    onClick={() => handleSelect('/goals')}
                    className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs text-slate-300 hover:text-slate-100 hover:bg-slate-800/70 transition-colors group text-left"
                  >
                    <div className="flex items-center gap-2.5 truncate">
                      <Target className="w-4 h-4 text-indigo-400 shrink-0" />
                      <span className="truncate">{goal.title}</span>
                    </div>
                    <span className="text-[10px] text-slate-500 shrink-0">{goal.progress}%</span>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Tasks */}
          {filteredTasks.length > 0 && (
            <div>
              <p className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Tasks</p>
              <div className="space-y-0.5">
                {filteredTasks.map((task) => (
                  <button
                    key={task.id}
                    onClick={() => handleSelect('/tasks')}
                    className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs text-slate-300 hover:text-slate-100 hover:bg-slate-800/70 transition-colors group text-left"
                  >
                    <div className="flex items-center gap-2.5 truncate">
                      <CheckSquare className="w-4 h-4 text-emerald-400 shrink-0" />
                      <span className="truncate">{task.title}</span>
                    </div>
                    <span className="text-[10px] text-slate-500 shrink-0">{task.status}</span>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Team Members */}
          {filteredUsers.length > 0 && (
            <div>
              <p className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Team Members</p>
              <div className="space-y-0.5">
                {filteredUsers.map((user) => (
                  <button
                    key={user.id}
                    onClick={() => handleSelect('/team')}
                    className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs text-slate-300 hover:text-slate-100 hover:bg-slate-800/70 transition-colors group text-left"
                  >
                    <div className="flex items-center gap-2.5 truncate">
                      <Users className="w-4 h-4 text-sky-400 shrink-0" />
                      <span className="truncate">{user.name}</span>
                    </div>
                    <span className="text-[10px] text-slate-500 shrink-0">{user.email}</span>
                  </button>
                ))}
              </div>
            </div>
          )}

          {quickNav.length === 0 && filteredGoals.length === 0 && filteredTasks.length === 0 && filteredUsers.length === 0 && (
            <div className="text-center py-6 text-xs text-slate-400">
              No matching results found for "{query}"
            </div>
          )}
        </div>

        {/* Footer info */}
        <div className="px-4 py-2 border-t border-slate-800/80 bg-slate-950/50 flex items-center justify-between text-[11px] text-slate-500">
          <span>Navigate with click or shortcuts</span>
          <span>DevSync Search</span>
        </div>
      </div>
    </div>
  );
};
