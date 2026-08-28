-- Night Tales Studio / Durable AI usage ledger
-- V2: immutable-ish provider usage events and normalized cost fields.

ALTER TABLE usage_records
    ADD COLUMN IF NOT EXISTS provider_request_id TEXT,
    ADD COLUMN IF NOT EXISTS currency TEXT NOT NULL DEFAULT 'USD',
    ADD COLUMN IF NOT EXISTS input_cost NUMERIC(18,8),
    ADD COLUMN IF NOT EXISTS output_cost NUMERIC(18,8),
    ADD COLUMN IF NOT EXISTS total_cost NUMERIC(18,8),
    ADD COLUMN IF NOT EXISTS metadata JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS idx_usage_task_created
    ON usage_records(task_id, created_at ASC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_usage_provider_request
    ON usage_records(provider, provider_request_id)
    WHERE provider_request_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_usage_provider_model_created
    ON usage_records(provider, model, created_at DESC);
