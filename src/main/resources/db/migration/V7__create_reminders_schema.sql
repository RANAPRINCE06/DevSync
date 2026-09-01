-- Migration V7: Create reminders table and indexes

CREATE TABLE IF NOT EXISTS reminders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000),
    reminder_time TIME NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_triggered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reminders_date_range CHECK (start_date <= end_date)
);

CREATE INDEX IF NOT EXISTS idx_reminders_user ON reminders(user_id);
CREATE INDEX IF NOT EXISTS idx_reminders_team ON reminders(team_id);
CREATE INDEX IF NOT EXISTS idx_reminders_user_active ON reminders(user_id, active);
CREATE INDEX IF NOT EXISTS idx_reminders_team_active ON reminders(team_id, active);
CREATE INDEX IF NOT EXISTS idx_reminders_time ON reminders(reminder_time);
