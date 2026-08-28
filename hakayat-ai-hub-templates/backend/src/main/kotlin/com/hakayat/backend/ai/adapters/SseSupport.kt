package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.AiStreamEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

internal suspend fun HttpClient.sseEvents(url: String, parse: (String, String) -> AiStreamEvent?): List<AiStreamEvent> {
    val body = get(url).bodyAsText()
    var event = "message"
    val out = mutableListOf<AiStreamEvent>()
    for (line in body.lines()) {
        when {
            line.startsWith("event:") -> event = line.substringAfter(':').trim()
            line.startsWith("data:") -> parse(event, line.substringAfter(':').trim())?.let(out::add)
        }
    }
    return out
}

internal fun text(json: String, key: String): String? =
    Regex("\\\\\"$key\\\\\"\\s*:\\s*\\\\\"((?:\\\\\\\\.|[^\\\\\"])*)\\\\\"")
        .find(json)?.groupValues?.get(1)?.replace("\\\\\\\"", "\\\"")

internal fun number(json: String, key: String): Long =
    Regex("\\\\\"$key\\\\\"\\s*:\\s*(\\\\d+)").find(json)?.groupValues?.get(1)?.toLong() ?: 0
