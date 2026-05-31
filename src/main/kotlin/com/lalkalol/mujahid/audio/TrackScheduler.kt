package com.lalkalol.mujahid.audio

import com.lalkalol.mujahid.util.Embeds
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason
import org.slf4j.LoggerFactory

const val DEFAULT_VOLUME = 50

/**
 * Per-guild queue and playback state machine. All queue mutations are guarded by [lock].
 */
class TrackScheduler(private val gm: GuildMusicManager) {
    private val log = LoggerFactory.getLogger(TrackScheduler::class.java)
    private val lock = Any()
    private val queue = mutableListOf<Track>()

    @Volatile
    var loopMode: LoopMode = LoopMode.OFF

    @Volatile
    var filter: AudioFilter = AudioFilter.OFF

    @Volatile
    var volume: Int = DEFAULT_VOLUME

    @Volatile
    var current: Track? = null
        private set

    fun queueSnapshot(): List<Track> = synchronized(lock) { queue.toList() }

    fun queueSize(): Int = synchronized(lock) { queue.size }

    fun enqueue(track: Track): Boolean {
        return if (current == null) {
            startTrack(track)
            true
        } else {
            synchronized(lock) { queue.add(track) }
            false
        }
    }

    fun enqueueAll(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        synchronized(lock) { queue.addAll(tracks) }
        if (current == null) {
            playNextOrStop()
        }
    }

    fun shuffle(): Boolean = synchronized(lock) {
        if (queue.size < 2) return false
        queue.shuffle()
        true
    }

    fun clearQueue() = synchronized(lock) { queue.clear() }

    fun removeAt(index: Int): Track? = synchronized(lock) {
        if (index in queue.indices) queue.removeAt(index) else null
    }

    /** Skips the current track, honouring queue loop. Returns the track that started, if any. */
    fun skip(): Track? {
        val cur = current
        if (loopMode == LoopMode.QUEUE && cur != null) {
            synchronized(lock) { queue.add(cur.makeClone()) }
        }
        return playNextOrStop()
    }

    fun stop() {
        clearQueue()
        current = null
        gm.link.createOrUpdatePlayer()
            .setPaused(false)
            .setTrack(null)
            .subscribe()
    }

    fun onTrackStart(track: Track) {
        current = track
        announceNowPlaying(track)
    }

    fun onTrackEnd(lastTrack: Track, endReason: AudioTrackEndReason) {
        // REPLACED/STOPPED reasons set mayStartNext=false; we must not advance then.
        if (!endReason.mayStartNext) return

        when (loopMode) {
            LoopMode.TRACK -> startTrack(lastTrack.makeClone())
            LoopMode.QUEUE -> {
                synchronized(lock) { queue.add(lastTrack.makeClone()) }
                playNextOrStop()
            }
            LoopMode.OFF -> playNextOrStop()
        }
    }

    private fun playNextOrStop(): Track? {
        val next = synchronized(lock) { if (queue.isEmpty()) null else queue.removeAt(0) }
        if (next != null) {
            startTrack(next)
        } else {
            current = null
            // No track queued: stop whatever is currently playing (e.g. when skipping the last track).
            gm.cachedLink?.createOrUpdatePlayer()?.setTrack(null)?.subscribe()
        }
        return next
    }

    private fun startTrack(track: Track) {
        current = track
        gm.link.createOrUpdatePlayer()
            .setTrack(track)
            .setVolume(volume)
            .setFilters(filter.build())
            .subscribe(
                {},
                { error -> log.error("Failed to start track in guild {}", gm.guildId, error) },
            )
    }

    fun applyVolume(newVolume: Int) {
        volume = newVolume.coerceIn(0, 200)
        gm.cachedLink?.createOrUpdatePlayer()?.setVolume(volume)?.subscribe()
    }

    fun applyFilter(newFilter: AudioFilter) {
        filter = newFilter
        gm.cachedLink?.createOrUpdatePlayer()?.setFilters(newFilter.build())?.subscribe()
    }

    private fun announceNowPlaying(track: Track) {
        val channel = resolveChannel(track) ?: return
        val info = track.info
        val embed = Embeds.music()
            .setAuthor("Now playing")
            .setTitle(info.title.ifBlank { "Unknown" }, info.uri)
            .setDescription("by ${info.author}")
            .build()
        channel.sendMessageEmbeds(embed).queue(null) { /* ignore send failures */ }
    }

    private fun resolveChannel(track: Track) = runCatching {
        val data = track.getUserData(TrackUserData::class.java)
        if (data.textChannelId == 0L) return@runCatching null
        gm.manager.jda?.getTextChannelById(data.textChannelId)
    }.getOrNull()
}
