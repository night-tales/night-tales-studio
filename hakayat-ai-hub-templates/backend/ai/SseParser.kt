package com.hakayat.backend.ai

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal suspend fun parseSse(
    channel: ByteReadChannel,
    onEvent: suspend (event: String?, data: String) -> Unit
) {
    var event: String? = null
    val data = StringBuilder()

    suspend fun dispatch() {
        if (data.isEmpty()) return
        onEvent(event, data.toString().removeSuffix("\n"))
        event = null
        data.clear()
    }

    while (!channel.isClosedForRead) {
        val line = channel.readUTF8Line() ?: break
        when {
            line.startsWith("event:") -> event = line.substringAfter(':').trim()
            line.startsWith("data:") -> { data.append(line.substringAfter(':').trim()); data.append('\n') }
            line.isBlank() -> dispatch()
        }
    }
    dispatch()
}

internal fun Json.parseObject(data: String): JsonObject = parseToJsonElement(data).jsonObject
