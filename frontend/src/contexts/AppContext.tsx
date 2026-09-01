import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/types/user';
import { Team } from '@/types/team';
import { userApi } from '@/services/userApi';
import { teamApi } from '@/services/teamApi';

interface AppContextType {
  activeUser: User | null;
  activeTeam: Team | null;
  users: User[];
  teams: Team[];
  setActiveUser: (user: User | null) => void;
  setActiveTeam: (team: Team | null) => void;
  isLoading: boolean;
  refreshUsersAndTeams: () => Promise<void>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [activeUser, setActiveUser] = useState<User | null>(null);
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
        const fetchedUsers = usersRes.value.content;
        setUsers(fetchedUsers);
        if (!activeUser) {
          setActiveUser(fetchedUsers[0]);
        }
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
  }, []);

  return (
    <AppContext.Provider
      value={{
        activeUser,
        activeTeam,
        users,
        teams,
        setActiveUser,
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
