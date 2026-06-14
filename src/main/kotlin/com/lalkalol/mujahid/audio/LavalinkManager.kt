package com.lalkalol.mujahid.audio

import com.lalkalol.mujahid.config.Config
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.getUserIdFromToken
import dev.arbjerg.lavalink.client.NodeOptions
import dev.arbjerg.lavalink.client.event.ReadyEvent
import dev.arbjerg.lavalink.client.event.StatsEvent
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackStartEvent
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider
import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class LavalinkManager(private val config: Config) {
    private val log = LoggerFactory.getLogger(LavalinkManager::class.java)
    val client: LavalinkClient = LavalinkClient(getUserIdFromToken(config.discordToken))
    private val musicManagers = ConcurrentHashMap<Long, GuildMusicManager>()

    @Volatile
    var jda: JDA? = null
        private set

    fun setJda(jda: JDA) {
        this.jda = jda
    }

    fun start() {
        client.loadBalancer.addPenaltyProvider(VoiceRegionPenaltyProvider())
        registerEventListeners()
        addNode()
    }

    fun getOrCreate(guildId: Long): GuildMusicManager =
        musicManagers.computeIfAbsent(guildId) { GuildMusicManager(it, this) }

    fun getExisting(guildId: Long): GuildMusicManager? = musicManagers[guildId]

    fun destroy(guildId: Long) {
        musicManagers.remove(guildId)
        log.debug("Destroyed music manager for guild {}", guildId)
    }

    fun getAllManagers(): List<Map.Entry<Long, GuildMusicManager>> =
        musicManagers.entries.toList()

    fun activePlayerCount(): Int = musicManagers.size

    fun shutdown() {
        log.info("Shutting down Lavalink client")
        client.close()
    }

    private fun addNode() {
        client.addNode(
            NodeOptions.Builder()
                .setName("main")
                .setServerUri(config.lavalinkUri())
                .setPassword(config.lavalinkPassword)
                .build(),
        )
    }

    private fun registerEventListeners() {
        client.on(ReadyEvent::class.java).subscribe { event ->
            log.info(
                "Lavalink node '{}' ready (session {})",
                event.node.name,
                event.sessionId,
            )
        }

        client.on(StatsEvent::class.java).subscribe { event ->
            log.debug(
                "Node '{}' players {}/{}",
                event.node.name,
                event.playingPlayers,
                event.players,
            )
        }

        client.on(TrackStartEvent::class.java).subscribe { event ->
            getExisting(event.guildId)?.scheduler?.onTrackStart(event.track)
        }

        client.on(TrackEndEvent::class.java).subscribe { event ->
            getExisting(event.guildId)?.scheduler?.onTrackEnd(event.track, event.endReason)
        }

        client.on(WebSocketClosedEvent::class.java).subscribe { event ->
            if (event.code == SESSION_INVALID) {
                reconnectVoice(event.guildId)
            }
        }
    }

    private fun reconnectVoice(guildId: Long) {
        val jdaInstance = jda ?: return
        val guild = jdaInstance.getGuildById(guildId) ?: return
        val channel = guild.selfMember.voiceState?.channel ?: return
        log.warn("Voice session invalid for guild {}, reconnecting", guildId)
        guild.jda.directAudioController.reconnect(channel)
    }

    companion object {
        private const val SESSION_INVALID = 4006
    }
}
