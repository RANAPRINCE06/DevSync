export type CodingPlatform = 'LEETCODE' | 'CODEFORCES' | 'CODECHEF' | 'HACKERRANK' | 'GITHUB' | 'OTHER';

export interface CodingProfile {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  platform: CodingPlatform;
  username: string;
  profileUrl?: string | null;
  rating?: number | null;
  problemsSolved?: number | null;
  contestsParticipated?: number | null;
  rank?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCodingProfileRequest {
  userId: string;
  platform: CodingPlatform;
  username: string;
  profileUrl?: string;
  rating?: number;
  problemsSolved?: number;
  contestsParticipated?: number;
  rank?: string;
}

export interface UpdateCodingProfileRequest {
  username: string;
  profileUrl?: string;
  rating?: number;
  problemsSolved?: number;
  contestsParticipated?: number;
  rank?: string;
  active?: boolean;
}

export interface CodingProfileFilterParams {
  userId?: string;
  platform?: CodingPlatform;
  active?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}
