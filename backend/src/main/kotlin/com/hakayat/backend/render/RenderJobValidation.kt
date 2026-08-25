package com.hakayat.backend.render

import java.util.UUID

object RenderJobValidation {
    fun projectId(projectId: UUID) { require(projectId != UUID(0L, 0L)) { "projectId is required" } }
    fun idempotencyKey(key: String?) { if (key != null) require(key.length <= 255) { "idempotency key too long" } }
}