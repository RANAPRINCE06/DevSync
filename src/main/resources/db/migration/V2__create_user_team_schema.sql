-- Migration V2: Transform baseline users and teams tables to UUID schema and add team_members table

-- Safely update users table
ALTER TABLE users DROP COLUMN IF EXISTS id;
ALTER TABLE users ADD COLUMN id UUID PRIMARY KEY;
ALTER TABLE users DROP COLUMN IF EXISTS username;
ALTER TABLE users RENAME COLUMN full_name TO name;
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ALTER COLUMN created_at SET DATA TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';
ALTER TABLE users ALTER COLUMN updated_at SET DATA TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';

-- Safely update teams table
ALTER TABLE teams DROP COLUMN IF EXISTS id;
ALTER TABLE teams ADD COLUMN id UUID PRIMARY KEY;
ALTER TABLE teams ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE teams ALTER COLUMN created_at SET DATA TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';
ALTER TABLE teams ALTER COLUMN updated_at SET DATA TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';

-- Create team_members table
CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_team_members_user_team UNIQUE (user_id, team_id)
);

-- Indexes for performance & search
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS idx_teams_name ON teams(name);
CREATE INDEX IF NOT EXISTS idx_team_members_user ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_team ON team_members(team_id);
