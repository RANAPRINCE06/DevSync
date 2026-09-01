import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types/user';
import { Team } from '@/types/team';
import { userApi } from '@/services/userApi';
import { teamApi } from '@/services/teamApi';
import { useAuth } from './AuthContext';

interface AppContextType {
  activeUser: User | null;
  activeTeam: Team | null;
  users: User[];
  teams: Team[];
  setActiveTeam: (team: Team | null) => void;
  isLoading: boolean;
  refreshUsersAndTeams: () => Promise<void>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { user: authUser } = useAuth();
  const [activeTeam, setActiveTeam] = useState<Team | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshUsersAndTeams = async () => {
    try {
      setIsLoading(true);
      const [usersRes, teamsRes] = await Promise.allSettled([
        userApi.getUsers({ size: 50 }),
        teamApi.getTeams({ size: 50 }),
      ]);

      if (usersRes.status === 'fulfilled' && usersRes.value.content.length > 0) {
        setUsers(usersRes.value.content);
      }

      if (teamsRes.status === 'fulfilled' && teamsRes.value.content.length > 0) {
        const fetchedTeams = teamsRes.value.content;
        setTeams(fetchedTeams);
        if (!activeTeam) {
          setActiveTeam(fetchedTeams[0]);
        }
      }
    } catch {
      // Handled gracefully
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    refreshUsersAndTeams();
  }, [authUser]);

  return (
    <AppContext.Provider
      value={{
        activeUser: authUser,
        activeTeam,
        users,
        teams,
        setActiveTeam,
        isLoading,
        refreshUsersAndTeams,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = (): AppContextType => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
