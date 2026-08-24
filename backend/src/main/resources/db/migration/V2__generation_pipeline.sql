create table if not exists characters (
  id uuid primary key,
  project_id uuid not null references projects(id) on delete cascade,
  name text not null,
  description text not null,
  visual_prompt text
);

create table if not exists media_assets (
  id uuid primary key,
  project_id uuid not null references projects(id) on delete cascade,
  scene_id uuid references scenes(id) on delete cascade,
  type text not null,
  uri text,
  mime_type text,
  status text not null default 'pending',
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create index if not exists idx_media_assets_project on media_assets(project_id);
create index if not exists idx_media_assets_scene on media_assets(scene_id);

create table if not exists timeline_clips (
  id uuid primary key,
  project_id uuid not null references projects(id) on delete cascade,
  track_type text not null,
  asset_id uuid references media_assets(id) on delete set null,
  start_ms bigint not null,
  duration_ms bigint not null,
  properties jsonb not null default '{}'::jsonb
);

create index if not exists idx_timeline_project on timeline_clips(project_id);
