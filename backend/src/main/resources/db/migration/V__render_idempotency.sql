CREATE TABLE IF NOT EXISTS render_job_idempotency (
    project_id UUID NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    job_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (project_id, idempotency_key)
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_render_job_idempotency_job_id ON render_job_idempotency(job_id);