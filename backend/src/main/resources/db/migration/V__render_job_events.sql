CREATE TABLE IF NOT EXISTS render_job_events (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL,
    progress SMALLINT NOT NULL,
    message TEXT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT render_job_events_progress_check CHECK (progress BETWEEN 0 AND 100)
);
CREATE INDEX IF NOT EXISTS idx_render_job_events_job_id_occurred_at ON render_job_events(job_id, occurred_at);