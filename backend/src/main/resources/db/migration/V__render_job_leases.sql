CREATE TABLE IF NOT EXISTS render_job_leases (
    job_id UUID PRIMARY KEY,
    worker_id VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_render_job_leases_expiry ON render_job_leases(expires_at);