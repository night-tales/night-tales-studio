create index if not exists idx_generation_jobs_status_created
  on generation_jobs(status, created_at);

create index if not exists idx_scenes_project_order
  on scenes(project_id, scene_order);

alter table generation_jobs
  add column if not exists attempt integer not null default 0;

alter table generation_jobs
  add column if not exists updated_at timestamptz not null default now();
