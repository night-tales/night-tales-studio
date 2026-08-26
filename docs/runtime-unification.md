# Runtime unification

The unified runtime is Kotlin/JVM with Gradle, composed of `core`, `backend`, and `android`.

Integration order: CI/build, PostgreSQL repositories and migrations, Redis queue and worker, LlamaGen adapter/webhook, then Cloud Run deployment and health checks.

Provider credentials remain environment/secret based and are never committed.
