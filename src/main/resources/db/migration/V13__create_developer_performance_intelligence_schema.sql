-- Migration V13: Create developer performance intelligence schema (achievements, contests, and github repositories)

CREATE TABLE IF NOT EXISTS platform_achievements (
    id UUID PRIMARY KEY,
    platform_account_id UUID NOT NULL REFERENCES platform_accounts(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    achievement_id VARCHAR(150) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    image_url VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    level VARCHAR(50),
    earned_date TIMESTAMP WITH TIME ZONE,
    source_url VARCHAR(500),
    last_verified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_platform_account_achievement UNIQUE (platform_account_id, achievement_id)
);

CREATE INDEX IF NOT EXISTS idx_platform_achievements_account ON platform_achievements(platform_account_id);
CREATE INDEX IF NOT EXISTS idx_platform_achievements_platform ON platform_achievements(platform);
CREATE INDEX IF NOT EXISTS idx_platform_achievements_category ON platform_achievements(category);
CREATE INDEX IF NOT EXISTS idx_platform_achievements_earned_date ON platform_achievements(earned_date DESC);

CREATE TABLE IF NOT EXISTS platform_contest_records (
    id UUID PRIMARY KEY,
    platform_account_id UUID NOT NULL REFERENCES platform_accounts(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    contest_id VARCHAR(150) NOT NULL,
    contest_name VARCHAR(250) NOT NULL,
    contest_date TIMESTAMP WITH TIME ZONE NOT NULL,
    contest_rank INTEGER,
    participants_count INTEGER,
    previous_rating DOUBLE PRECISION,
    rating_change DOUBLE PRECISION,
    new_rating DOUBLE PRECISION NOT NULL,
    problems_solved INTEGER,
    performance_score DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_platform_account_contest UNIQUE (platform_account_id, contest_id)
);

CREATE INDEX IF NOT EXISTS idx_contest_records_account ON platform_contest_records(platform_account_id);
CREATE INDEX IF NOT EXISTS idx_contest_records_platform ON platform_contest_records(platform);
CREATE INDEX IF NOT EXISTS idx_contest_records_date ON platform_contest_records(platform_account_id, contest_date DESC);

CREATE TABLE IF NOT EXISTS github_repository_records (
    id UUID PRIMARY KEY,
    platform_account_id UUID NOT NULL REFERENCES platform_accounts(id) ON DELETE CASCADE,
    repo_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    primary_language VARCHAR(100),
    stars_count INTEGER NOT NULL DEFAULT 0,
    forks_count INTEGER NOT NULL DEFAULT 0,
    recent_commits_count INTEGER NOT NULL DEFAULT 0,
    last_updated_at TIMESTAMP WITH TIME ZONE,
    repo_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_platform_account_repo UNIQUE (platform_account_id, repo_name)
);

CREATE INDEX IF NOT EXISTS idx_github_repos_account ON github_repository_records(platform_account_id);
CREATE INDEX IF NOT EXISTS idx_github_repos_stars ON github_repository_records(platform_account_id, stars_count DESC);
CREATE INDEX IF NOT EXISTS idx_github_repos_updated ON github_repository_records(platform_account_id, last_updated_at DESC);
