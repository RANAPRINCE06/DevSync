import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { DashboardPage } from '@/pages/Dashboard/DashboardPage';
import { ProgressPage } from '@/pages/Progress/ProgressPage';
import { GoalsPage } from '@/pages/Goals/GoalsPage';
import { TasksPage } from '@/pages/Tasks/TasksPage';
import { TeamPage } from '@/pages/Team/TeamPage';
import { LeaderboardPage } from '@/pages/Leaderboard/LeaderboardPage';
import { AchievementsPage } from '@/pages/Achievements/AchievementsPage';
import { CodingProfilesPage } from '@/pages/CodingProfiles/CodingProfilesPage';
import { NotificationsPage } from '@/pages/Notifications/NotificationsPage';
import { SettingsPage } from '@/pages/Settings/SettingsPage';

export const router = createBrowserRouter([
  {
    path: '/',
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
]);
