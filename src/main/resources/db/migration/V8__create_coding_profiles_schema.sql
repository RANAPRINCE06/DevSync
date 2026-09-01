-- Migration V8: Create coding_profiles table, constraints and indexes

CREATE TABLE IF NOT EXISTS coding_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    profile_url VARCHAR(500),
    rating INTEGER,
    problems_solved INTEGER,
    contests_participated INTEGER,
    rank VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_coding_profiles_rating CHECK (rating IS NULL OR rating >= 0),
    CONSTRAINT chk_coding_profiles_problems_solved CHECK (problems_solved IS NULL OR problems_solved >= 0),
    CONSTRAINT chk_coding_profiles_contests_participated CHECK (contests_participated IS NULL OR contests_participated >= 0)
);

CREATE INDEX IF NOT EXISTS idx_coding_profiles_user ON coding_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_coding_profiles_platform ON coding_profiles(platform);
CREATE INDEX IF NOT EXISTS idx_coding_profiles_user_platform ON coding_profiles(user_id, platform);
CREATE INDEX IF NOT EXISTS idx_coding_profiles_active ON coding_profiles(active);
