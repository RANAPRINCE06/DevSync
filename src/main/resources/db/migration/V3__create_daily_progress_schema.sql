-- Migration V3: Create daily_progress table and performance indexes

CREATE TABLE IF NOT EXISTS daily_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    progress_date DATE NOT NULL,
    what_studied VARCHAR(2000) NOT NULL,
    completed VARCHAR(2000) NOT NULL,
    study_minutes INTEGER NOT NULL,
    challenges VARCHAR(2000),
    improvement_areas VARCHAR(2000),
    tomorrow_plan VARCHAR(2000),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_progress_user_team_date UNIQUE (user_id, team_id, progress_date)
);

-- Indexes for fast filtering and reporting
CREATE INDEX IF NOT EXISTS idx_daily_progress_user ON daily_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_daily_progress_team ON daily_progress(team_id);
CREATE INDEX IF NOT EXISTS idx_daily_progress_date ON daily_progress(progress_date);
CREATE INDEX IF NOT EXISTS idx_daily_progress_team_date ON daily_progress(team_id, progress_date);
CREATE INDEX IF NOT EXISTS idx_daily_progress_user_date ON daily_progress(user_id, progress_date);
