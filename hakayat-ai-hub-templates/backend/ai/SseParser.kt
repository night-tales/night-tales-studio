package com.hakayat.backend.ai

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal fun parseSse(
    channel: ByteReadChannel,
    json: Json,
    onEvent: suspend (event: String?, data: String) -> Unit
): Flow<AiStreamEvent> = flow {
    var event: String? = null
    val data = StringBuilder()

    suspend fun dispatch() {
        if (data.isEmpty()) return
        val payload = data.toString().removeSuffix("\n")
        onEvent(event, payload)
        event = null
        data.clear()
    }

    while (!channel.isClosedForRead) {
        val line = channel.readUTF8Line() ?: break
        when {
            line.startsWith("event:") -> event = line.substringAfter(':').trim()
            line.startsWith("data:") -> {
                data.append(line.substringAfter(':').trim())
                data.append('\n')
            }
            line.isBlank() -> dispatch()
        }
    }
    dispatch()
}

internal fun Json.parseObject(data: String): JsonObject =
    parseToJsonElement(data).jsonObject
