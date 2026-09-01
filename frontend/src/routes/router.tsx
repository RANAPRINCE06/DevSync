import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { LoginPage } from '@/pages/Auth/LoginPage';
import { RegisterPage } from '@/pages/Auth/RegisterPage';
import { ForgotPasswordPage } from '@/pages/Auth/ForgotPasswordPage';
import { DashboardPage } from '@/pages/Dashboard/DashboardPage';
import { ProgressPage } from '@/pages/Progress/ProgressPage';
import { GoalsPage } from '@/pages/Goals/GoalsPage';
import { TasksPage } from '@/pages/Tasks/TasksPage';
import { TeamPage } from '@/pages/Team/TeamPage';
import { LeaderboardPage } from '@/pages/Leaderboard/LeaderboardPage';
import { AchievementsPage } from '@/pages/Achievements/AchievementsPage';
import { CodingProfilesPage } from '@/pages/CodingProfiles/CodingProfilesPage';
import { AnalyticsPage } from '@/pages/Analytics/AnalyticsPage';
import { RemindersPage } from '@/pages/Reminders/RemindersPage';
import { NotificationsPage } from '@/pages/Notifications/NotificationsPage';
import { SettingsPage } from '@/pages/Settings/SettingsPage';
import { useAuth } from '@/hooks/useAuth';

// Public Route Guard (Redirects authenticated users away from Login/Register to Dashboard)
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return null;
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

export const router = createBrowserRouter([
  // Public Authentication Routes
  {
    path: '/login',
    element: (
      <PublicRoute>
        <LoginPage />
      </PublicRoute>
    ),
  },
  {
    path: '/register',
    element: (
      <PublicRoute>
        <RegisterPage />
      </PublicRoute>
    ),
  },
  {
    path: '/forgot-password',
    element: (
      <PublicRoute>
        <ForgotPasswordPage />
      </PublicRoute>
    ),
  },

  // Protected Application Routes
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          {
            index: true,
            element: <Navigate to="/dashboard" replace />,
          },
          {
            path: 'dashboard',
            element: <DashboardPage />,
          },
          {
            path: 'progress',
            element: <ProgressPage />,
          },
          {
            path: 'goals',
            element: <GoalsPage />,
          },
          {
            path: 'tasks',
            element: <TasksPage />,
          },
          {
            path: 'team',
            element: <TeamPage />,
          },
          {
            path: 'leaderboard',
            element: <LeaderboardPage />,
          },
          {
            path: 'achievements',
            element: <AchievementsPage />,
          },
          {
            path: 'coding-profiles',
            element: <CodingProfilesPage />,
          },
          {
            path: 'analytics',
            element: <AnalyticsPage />,
          },
          {
            path: 'reminders',
            element: <RemindersPage />,
          },
          {
            path: 'notifications',
            element: <NotificationsPage />,
          },
          {
            path: 'settings',
            element: <SettingsPage />,
          },
          {
            path: '*',
            element: <Navigate to="/dashboard" replace />,
          },
        ],
      },
    ],
  },

  // Catch-all
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />,
  },
]);
