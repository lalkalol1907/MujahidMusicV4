package com.lalkalol.mujahid.internal

import com.lalkalol.mujahid.audio.GuildMusicManager
import com.lalkalol.mujahid.audio.LavalinkManager
import com.lalkalol.mujahid.internal.dto.NodeDto
import com.lalkalol.mujahid.internal.dto.QueueTrackDto
import com.lalkalol.mujahid.internal.dto.SessionDto
import com.lalkalol.mujahid.internal.dto.SessionsResponse
import dev.arbjerg.lavalink.client.player.Track
import org.slf4j.LoggerFactory

class LavalinkStateService(private val lavalinkManager: LavalinkManager) {
    private val log = LoggerFactory.getLogger(LavalinkStateService::class.java)

    fun getSessions(): SessionsResponse {
        return try {
            val nodes = lavalinkManager.client.nodes
            val nodeDtos = nodes.map { node ->
                val stats = node.stats
                NodeDto(
                    name = node.name,
                    uri = node.baseUri,
                    connected = node.available,
                    sessionId = node.sessionId,
                    players = stats?.players ?: 0,
                    playing = stats?.playingPlayers ?: 0,
                )
            }

            val jda = lavalinkManager.jda
            val sessions = mutableListOf<SessionDto>()

            for (node in nodes) {
                for ((guildId, gm) in lavalinkManager.getAllManagers()) {
                    val link = gm.getCachedLink() ?: continue

                    val nodeName = try {
                        link.node.name
                    } catch (_: Exception) {
                        "unknown"
                    }
                    if (nodeName != node.name) {
                        continue
                    }

                    val scheduler = gm.scheduler
                    val current = scheduler.getCurrent()
                    val player = gm.getPlayer()

                    var guildName = guildId.toString()
                    if (jda != null) {
                        jda.getGuildById(guildId)?.let { guildName = it.name }
                    }

                    var trackTitle: String? = null
                    var trackAuthor: String? = null
                    var lengthMs = 0L
                    if (current != null) {
                        val info = current.info
                        trackTitle = info.title
                        trackAuthor = info.author
                        lengthMs = info.length
                    }

                    var positionMs = 0L
                    var paused = false
                    var volume = 100
                    if (player != null) {
                        positionMs = player.position
                        paused = player.paused
                        volume = player.volume
                    }

                    sessions.add(
                        SessionDto(
                            guildId = guildId.toString(),
                            guildName = guildName,
                            nodeName = nodeName,
                            trackTitle = trackTitle,
                            trackAuthor = trackAuthor,
                            positionMs = positionMs,
                            lengthMs = lengthMs,
                            paused = paused,
                            volume = volume,
                            queueSize = scheduler.queueSize(),
                            loopMode = scheduler.getLoopMode().name.lowercase(),
                        ),
                    )
                }
            }

            SessionsResponse(nodeDtos, sessions)
        } catch (e: Exception) {
            log.error("Failed to build sessions response", e)
            SessionsResponse(emptyList(), emptyList())
        }
    }

    fun getQueue(guildId: Long): List<QueueTrackDto>? {
        val gm = lavalinkManager.getExisting(guildId) ?: return null

        val tracks = mutableListOf<QueueTrackDto>()
        val current = gm.scheduler.getCurrent()
        if (current != null) {
            val info = current.info
            tracks.add(
                QueueTrackDto(
                    position = 0,
                    title = blankToUnknown(info.title),
                    author = info.author ?: "",
                    uri = info.uri ?: "",
                    durationMs = info.length,
                ),
            )
        }

        var position = 1
        for (track in gm.scheduler.queueSnapshot()) {
            val info = track.info
            tracks.add(
                QueueTrackDto(
                    position = position++,
                    title = blankToUnknown(info.title),
                    author = info.author ?: "",
                    uri = info.uri ?: "",
                    durationMs = info.length,
                ),
            )
        }
        return tracks
    }

    private fun blankToUnknown(title: String?): String =
        if (title.isNullOrBlank()) "Unknown" else title
}
