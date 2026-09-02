import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types/user';
import { Team } from '@/types/team';
import { userApi } from '@/services/userApi';
import { teamApi } from '@/services/teamApi';
import { filterRealUsers } from '@/lib/userFilter';
import { useAuth } from './AuthContext';

interface AppContextType {
  activeUser: User | null;
  activeTeam: Team | null;
  users: User[];
  teams: Team[];
  setActiveTeam: (team: Team | null) => void;
  isLoading: boolean;
  refreshUsersAndTeams: () => Promise<void>;
  createTeam: (name: string, description?: string) => Promise<Team>;
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
        userApi.getUsers({ size: 1000 }),
        teamApi.getTeams({ size: 1000 }),
      ]);

      if (usersRes.status === 'fulfilled' && usersRes.value.content) {
        const realUsers = filterRealUsers(usersRes.value.content);
        setUsers(realUsers);
      }

      if (teamsRes.status === 'fulfilled' && teamsRes.value.content) {
        const fetchedTeams = teamsRes.value.content;
        setTeams(fetchedTeams);
        if (fetchedTeams.length > 0) {
          setActiveTeam((prev) => {
            if (!prev) return fetchedTeams[0];
            const stillExists = fetchedTeams.find((t) => t.id === prev.id);
            return stillExists || fetchedTeams[0];
          });
        }
      }
    } catch {
      // Handled gracefully
    } finally {
      setIsLoading(false);
    }
  };

  const createTeam = async (name: string, description?: string): Promise<Team> => {
    if (!authUser) {
      throw new Error('You must be logged in to create a team.');
    }
    const newTeam = await teamApi.createTeam({
      name,
      description,
      creatorUserId: authUser.id,
    });
    await refreshUsersAndTeams();
    setActiveTeam(newTeam);
    return newTeam;
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
        createTeam,
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
