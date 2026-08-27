-- Night Tales Studio / PostgreSQL foundation
-- V1: identity, conversations, tasks and artifacts.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- Secrets are intentionally NOT stored in plaintext here.

CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT,
    display_name TEXT,
    role TEXT NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS agents (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    provider TEXT NOT NULL,
    model TEXT NOT NULL,
    config JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('system', 'user', 'assistant', 'tool')),
    content TEXT NOT NULL,
    agent_id TEXT REFERENCES agents(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES conversations(id) ON DELETE SET NULL,
    agent_id TEXT REFERENCES agents(id) ON DELETE SET NULL,
    status TEXT NOT NULL CHECK (status IN ('QUEUED', 'RUNNING', 'PAUSED', 'COMPLETED', 'FAILED', 'CANCELLED')),
    input JSONB NOT NULL DEFAULT '{}'::jsonb,
    output JSONB,
    progress REAL NOT NULL DEFAULT 0 CHECK (progress >= 0 AND progress <= 1),
    error TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS task_events (
    id BIGSERIAL PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS provider_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider TEXT NOT NULL,
    secret_ref TEXT,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, provider)
);

CREATE TABLE IF NOT EXISTS realtime_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_realtime_tickets_expiry ON realtime_tickets(expires_at);

CREATE TABLE IF NOT EXISTS usage_records (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id UUID REFERENCES tasks(id) ON DELETE SET NULL,
    provider TEXT NOT NULL,
    model TEXT NOT NULL,
    input_tokens INTEGER NOT NULL DEFAULT 0,
    output_tokens INTEGER NOT NULL DEFAULT 0,
    latency_ms INTEGER,
    estimated_cost NUMERIC(18,8),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id UUID REFERENCES tasks(id) ON DELETE SET NULL,
    type TEXT NOT NULL,
    uri TEXT,
    content JSONB,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_conversations_user_updated
    ON conversations(user_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created
    ON messages(conversation_id, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_tasks_user_created
    ON tasks(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tasks_status_created
    ON tasks(status, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_tasks_claimable
    ON tasks(status, next_attempt_at, created_at)
    WHERE status = 'QUEUED';

CREATE UNIQUE INDEX IF NOT EXISTS idx_tasks_user_idempotency
    ON tasks(user_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_task_events_task_created
    ON task_events(task_id, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_usage_user_created
    ON usage_records(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_artifacts_user_created
    ON artifacts(user_id, created_at DESC);


CREATE INDEX IF NOT EXISTS idx_tasks_lease_expiry
    ON tasks(lease_expires_at)
    WHERE status = 'RUNNING';
