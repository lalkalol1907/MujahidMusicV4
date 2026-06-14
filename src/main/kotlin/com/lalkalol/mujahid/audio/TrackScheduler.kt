package com.lalkalol.mujahid.audio

import com.lalkalol.mujahid.util.Embeds
import com.lalkalol.mujahid.util.MusicControls
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.slf4j.LoggerFactory

class TrackScheduler(private val gm: GuildMusicManager) {
    private val log = LoggerFactory.getLogger(TrackScheduler::class.java)
    private val mutex = Mutex()
    private val queue = mutableListOf<Track>()

    @Volatile
    private var loopModeValue: LoopMode = LoopMode.OFF

    @Volatile
    private var filter: AudioFilter = AudioFilter.OFF

    @Volatile
    private var volume: Int = DEFAULT_VOLUME

    @Volatile
    private var current: Track? = null

    fun getLoopMode(): LoopMode = loopModeValue

    fun setLoopMode(loopMode: LoopMode) {
        this.loopModeValue = loopMode
    }

    fun getFilter(): AudioFilter = filter

    fun getVolume(): Int = volume

    fun getCurrent(): Track? = current

    fun queueSnapshot(): List<Track> = locked { queue.toList() }

    fun queueSize(): Int = locked { queue.size }

    fun isQueueFull(): Boolean = locked { queue.size >= MAX_QUEUE_SIZE }

    fun enqueue(track: Track): AddResult = locked {
        if (current == null) {
            startTrackLocked(track, false)
            AddResult.STARTED_NOW
        } else if (queue.size >= MAX_QUEUE_SIZE) {
            log.warn("Queue full in guild {} ({} tracks)", gm.guildId, MAX_QUEUE_SIZE)
            AddResult.REJECTED_FULL
        } else {
            queue.add(track)
            log.debug(
                "Enqueued '{}' in guild {} (position {})",
                track.info.title,
                gm.guildId,
                queue.size,
            )
            AddResult.QUEUED
        }
    }

    fun enqueueNext(track: Track): AddResult = locked {
        if (current == null) {
            startTrackLocked(track, false)
            AddResult.STARTED_NOW
        } else if (queue.size >= MAX_QUEUE_SIZE) {
            log.warn("Queue full in guild {} ({} tracks)", gm.guildId, MAX_QUEUE_SIZE)
            AddResult.REJECTED_FULL
        } else {
            queue.add(0, track)
            log.debug("Inserted next '{}' in guild {}", track.info.title, gm.guildId)
            AddResult.QUEUED
        }
    }

    fun enqueueAll(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            return
        }
        locked {
            val space = MAX_QUEUE_SIZE - queue.size
            val toAdd = if (tracks.size <= space) tracks else tracks.subList(0, maxOf(0, space))
            if (toAdd.isEmpty()) {
                log.warn("Queue full in guild {}, dropped {} tracks", gm.guildId, tracks.size)
                return@locked
            }
            if (toAdd.size < tracks.size) {
                log.warn(
                    "Truncated bulk enqueue in guild {} to {} tracks (limit {})",
                    gm.guildId,
                    toAdd.size,
                    MAX_QUEUE_SIZE,
                )
            }
            queue.addAll(toAdd)
            if (current == null) {
                playNextOrStopLocked(false)
            }
        }
    }

    fun shuffle(): Boolean = locked {
        if (queue.size < 2) {
            false
        } else {
            queue.shuffle()
            true
        }
    }

    fun clearQueue() {
        locked { queue.clear() }
    }

    fun removeAt(index: Int): Track? = locked {
        if (index in queue.indices) queue.removeAt(index) else null
    }

    fun skip(): Track? = locked {
        val cur = current
        if (loopModeValue == LoopMode.QUEUE && cur != null) {
            queue.add(cur.makeClone())
        }
        playNextOrStopLocked(true)
    }

    fun stop() {
        locked {
            queue.clear()
            current = null
        }
        gm.getLink().createOrUpdatePlayer()
            .setPaused(false)
            .setTrack(null)
            .subscribe()
        log.info("Stopped playback in guild {}", gm.guildId)
    }

    fun onTrackStart(track: Track) {
        locked { current = track }
    }

    fun onTrackEnd(lastTrack: Track, endReason: AudioTrackEndReason) {
        if (!endReason.mayStartNext) {
            log.debug("Track end in guild {} will not advance (reason {})", gm.guildId, endReason)
            return
        }

        locked {
            when (loopModeValue) {
                LoopMode.TRACK -> startTrackLocked(lastTrack.makeClone(), true)
                LoopMode.QUEUE -> {
                    queue.add(lastTrack.makeClone())
                    playNextOrStopLocked(true)
                }
                LoopMode.OFF -> playNextOrStopLocked(true)
            }
        }
    }

    fun applyVolume(newVolume: Int) {
        volume = newVolume.coerceIn(0, 200)
        gm.getCachedLink()?.createOrUpdatePlayer()?.setVolume(volume)?.subscribe()
    }

    fun applyFilter(newFilter: AudioFilter) {
        filter = newFilter
        gm.getCachedLink()?.createOrUpdatePlayer()?.setFilters(newFilter.build())?.subscribe()
    }

    private fun playNextOrStopLocked(announce: Boolean): Track? {
        val next = if (queue.isEmpty()) null else queue.removeAt(0)
        if (next != null) {
            startTrackLocked(next, announce)
        } else {
            current = null
            gm.getCachedLink()?.createOrUpdatePlayer()?.setTrack(null)?.subscribe()
            log.debug("Queue drained in guild {}", gm.guildId)
        }
        return next
    }

    private fun startTrackLocked(track: Track, announce: Boolean) {
        current = track
        log.info("Starting '{}' in guild {}", track.info.title, gm.guildId)
        gm.getLink().createOrUpdatePlayer()
            .setTrack(track)
            .setVolume(volume)
            .setFilters(filter.build())
            .subscribe(
                { },
                { error -> log.error("Failed to start track in guild {}", gm.guildId, error) },
            )
        if (announce) {
            announceNowPlaying(track)
        }
    }

    private fun announceNowPlaying(track: Track) {
        val channel = resolveChannel(track) ?: return
        val info = track.info
        val embed = Embeds.music()
            .setAuthor("Now playing")
            .setTitle(if (info.title.isBlank()) "Unknown" else info.title, info.uri)
            .setDescription("by ${info.author}")
            .build()
        val paused = gm.getPlayer()?.paused == true
        channel.sendMessageEmbeds(embed)
            .setComponents(MusicControls.playbackRow(gm.guildId, paused))
            .queue(null) { failure ->
                log.warn("Failed to announce track in guild {}", gm.guildId, failure)
            }
    }

    private fun resolveChannel(track: Track): MessageChannel? {
        return try {
            val data = track.getUserData(TrackUserData::class.java)
            if (data == null || data.textChannelId == 0L) {
                return null
            }
            val jda = gm.getManager().jda ?: return null
            jda.getTextChannelById(data.textChannelId)
        } catch (e: Exception) {
            log.debug("Could not resolve announce channel in guild {}", gm.guildId, e)
            null
        }
    }

    private inline fun <T> locked(crossinline block: suspend () -> T): T =
        runBlocking { mutex.withLock { block() } }

    companion object {
        const val DEFAULT_VOLUME: Int = 50
        const val MAX_QUEUE_SIZE: Int = 500
    }
}
