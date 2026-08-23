# Night Tales Studio Architecture

## Pipeline
Idea -> Blueprint -> Scenes -> parallel image/voice generation -> subtitles -> quality checks -> timeline -> preview -> render -> export -> library/share.

## Modules
- `android`: Compose UI, local persistence, WorkManager and Media3 integration.
- `core`: serializable domain contracts and media timeline models.
- `backend`: Ktor API, orchestration, persistence and job dispatch.
- `infra`: PostgreSQL/Redis local services and deployment assets.

## AI
Agents depend on `LlmProvider`; credentials are supplied through environment/secret storage only.

## Jobs
The API creates jobs. Workers execute long-running generation and rendering work. Job state is persisted and exposed to Android for progress tracking.
