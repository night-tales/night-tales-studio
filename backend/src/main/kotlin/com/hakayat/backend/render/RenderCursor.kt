package com.hakayat.backend.render

import java.util.Base64

object RenderCursor {
    fun encode(value: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())
    fun decode(value: String): String = runCatching { String(Base64.getUrlDecoder().decode(value)) }.getOrElse { throw IllegalArgumentException("invalid cursor") }
}