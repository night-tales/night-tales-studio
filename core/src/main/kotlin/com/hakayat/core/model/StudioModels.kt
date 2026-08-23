package com.hakayat.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Blueprint(val title: String, val logline: String, val genre: String, val tone: String, val sceneCount: Int)

@Serializable
data class Character(val id: String, val name: String, val description: String, val visualPrompt: String)

@Serializable
data class MediaAsset(val id: String, val projectId: String, val sceneId: String? = null, val kind: AssetKind, val uri: String, val durationMs: Long = 0)

enum class AssetKind { IMAGE, AUDIO, VIDEO, SUBTITLE }

@Serializable
data class Timeline(val projectId: String, val tracks: List<TimelineTrack> = emptyList())

@Serializable
data class TimelineTrack(val id: String, val type: TrackType, val clips: List<TimelineClip> = emptyList())

enum class TrackType { VIDEO, AUDIO, SUBTITLE }

@Serializable
data class TimelineClip(val id: String, val assetId: String, val startMs: Long, val durationMs: Long)

@Serializable
data class JobProgress(val jobId: String, val status: String, val progress: Int, val message: String? = null)
