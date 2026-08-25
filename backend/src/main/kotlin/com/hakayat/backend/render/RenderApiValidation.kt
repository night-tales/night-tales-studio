package com.hakayat.backend.render

import java.util.UUID

object RenderApiValidation {
    fun uuid(value: String?, field: String): UUID =
        value?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: throw IllegalArgumentException("invalid $field")

    fun idempotencyKey(value: String?): String =
        value?.trim()?.takeIf { it.isNotEmpty() && it.length <= 128 }
            ?: throw IllegalArgumentException("invalid idempotency key")
}