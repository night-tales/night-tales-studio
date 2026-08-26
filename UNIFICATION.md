# Runtime unification

The unified application runtime is Kotlin/JVM with Gradle, composed of `core`, `backend`, and `android`.

## Integration order

1. Gradle/CI build
2. PostgreSQL repositories and migrations
3. Redis queue and worker
4. LlamaGen adapter and webhook
5. Cloud Run deployment and health checks

Provider credentials remain environment/secret based and must never be committed.
