package com.lalkalol.mujahid.internal.dto

import kotlinx.serialization.Serializable

@Serializable
data class GuildDto(
    val id: String,
    val name: String,
    val memberCount: Int,
)

@Serializable
data class NodeDto(
    val name: String,
    val uri: String,
    val connected: Boolean,
    val sessionId: String?,
    val players: Int,
    val playing: Int,
)

@Serializable
data class QueueTrackDto(
    val position: Int,
    val title: String,
    val author: String,
    val uri: String,
    val durationMs: Long,
)

@Serializable
data class SessionDto(
    val guildId: String,
    val guildName: String,
    val nodeName: String,
    val trackTitle: String?,
    val trackAuthor: String?,
    val positionMs: Long,
    val lengthMs: Long,
    val paused: Boolean,
    val volume: Int,
    val queueSize: Int,
    val loopMode: String,
)

@Serializable
data class SessionsResponse(
    val nodes: List<NodeDto>,
    val sessions: List<SessionDto>,
)

@Serializable
data class HealthResponse(
    val status: String,
)

@Serializable
data class ErrorResponse(
    val error: String,
)

@Serializable
data class ActionResponse(
    val status: String,
)
