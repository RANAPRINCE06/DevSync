-- Migration V11: Add collaborative team resources flags for goals and tasks

ALTER TABLE goals ADD COLUMN IF NOT EXISTS team_goal BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_goals_team_goal ON goals(team_goal);

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS team_task BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_tasks_team_task ON tasks(team_task);

