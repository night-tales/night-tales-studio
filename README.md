# Night Tales Studio

AI-powered creative studio for story generation, media production, timeline editing, rendering and export.

## Status

The repository is being built as an independent full-stack project. The target pipeline is:

**Idea → Blueprint → Scenes → Images + Voice → Subtitles → Quality → Timeline → Preview → MP4 → Library → Share**

## Stack

- Android: Kotlin, Jetpack Compose, WorkManager, Media3
- Backend: Ktor, Kotlin
- Data: PostgreSQL, Redis
- AI: provider abstraction + orchestration agents
- Infrastructure: Docker Compose, GitHub Actions

## Security

API keys and production credentials are never committed. Use `.env` locally or repository/environment secrets in deployment.

See `docs/ARCHITECTURE.md` for the system design.
