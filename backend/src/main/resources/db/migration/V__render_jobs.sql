CREATE TABLE IF NOT EXISTS render_jobs (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL,
    progress SMALLINT NOT NULL DEFAULT 0,
    output_asset_id UUID NULL,
    error TEXT NULL,
    attempt INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT render_jobs_progress_check CHECK (progress BETWEEN 0 AND 100),
    CONSTRAINT render_jobs_attempt_check CHECK (attempt >= 0),
    CONSTRAINT render_jobs_status_check CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_render_jobs_project_id ON render_jobs(project_id);
CREATE INDEX IF NOT EXISTS idx_render_jobs_status ON render_jobs(status);
