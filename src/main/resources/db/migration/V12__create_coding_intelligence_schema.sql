-- Migration V12: Create coding intelligence platform_accounts and platform_stat_snapshots

CREATE TABLE IF NOT EXISTS platform_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    profile_url VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sync_status VARCHAR(30) NOT NULL DEFAULT 'NEVER_SYNCED',
    last_synced_at TIMESTAMP WITH TIME ZONE,
    last_sync_attempt_at TIMESTAMP WITH TIME ZONE,
    last_sync_error VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_platform_accounts_user ON platform_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_platform_accounts_platform ON platform_accounts(platform);
CREATE INDEX IF NOT EXISTS idx_platform_accounts_user_platform ON platform_accounts(user_id, platform);
CREATE INDEX IF NOT EXISTS idx_platform_accounts_active ON platform_accounts(active);
CREATE INDEX IF NOT EXISTS idx_platform_accounts_sync_status ON platform_accounts(sync_status);

CREATE TABLE IF NOT EXISTS platform_stat_snapshots (
    id UUID PRIMARY KEY,
    platform_account_id UUID NOT NULL REFERENCES platform_accounts(id) ON DELETE CASCADE,
    current_rating DOUBLE PRECISION,
    maximum_rating DOUBLE PRECISION,
    global_rank BIGINT,
    country_rank BIGINT,
    problems_solved INTEGER,
    easy_solved INTEGER,
    medium_solved INTEGER,
    hard_solved INTEGER,
    contests_participated INTEGER,
    additional_stats JSON,
    snapshot_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_stat_snapshots_problems CHECK (problems_solved IS NULL OR problems_solved >= 0),
    CONSTRAINT chk_stat_snapshots_easy CHECK (easy_solved IS NULL OR easy_solved >= 0),
    CONSTRAINT chk_stat_snapshots_medium CHECK (medium_solved IS NULL OR medium_solved >= 0),
    CONSTRAINT chk_stat_snapshots_hard CHECK (hard_solved IS NULL OR hard_solved >= 0),
    CONSTRAINT chk_stat_snapshots_contests CHECK (contests_participated IS NULL OR contests_participated >= 0)
);

CREATE INDEX IF NOT EXISTS idx_stat_snapshots_account ON platform_stat_snapshots(platform_account_id);
CREATE INDEX IF NOT EXISTS idx_stat_snapshots_account_snapshot_at ON platform_stat_snapshots(platform_account_id, snapshot_at DESC);
CREATE INDEX IF NOT EXISTS idx_stat_snapshots_created_at ON platform_stat_snapshots(created_at);
