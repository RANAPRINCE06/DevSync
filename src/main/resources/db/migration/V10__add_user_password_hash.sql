-- Migration V10: Add password_hash column to users table for secure password authentication
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
