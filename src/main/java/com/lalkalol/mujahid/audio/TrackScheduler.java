package com.lalkalol.mujahid.audio;

import com.lalkalol.mujahid.util.Embeds;
import com.lalkalol.mujahid.util.MusicControls;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackScheduler {
    public static final int DEFAULT_VOLUME = 50;
    public static final int MAX_QUEUE_SIZE = 500;

    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    private final GuildMusicManager gm;
    private final Object lock = new Object();
    private final List<Track> queue = new ArrayList<>();

    private volatile LoopMode loopMode = LoopMode.OFF;
    private volatile AudioFilter filter = AudioFilter.OFF;
    private volatile int volume = DEFAULT_VOLUME;
    private volatile Track current;

    public TrackScheduler(GuildMusicManager gm) {
        this.gm = gm;
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }

    public AudioFilter getFilter() {
        return filter;
    }

    public int getVolume() {
        return volume;
    }

    public Track getCurrent() {
        return current;
    }

    public List<Track> queueSnapshot() {
        synchronized (lock) {
            return List.copyOf(queue);
        }
    }

    public int queueSize() {
        synchronized (lock) {
            return queue.size();
        }
    }

    public boolean isQueueFull() {
        synchronized (lock) {
            return queue.size() >= MAX_QUEUE_SIZE;
        }
    }

    public AddResult enqueue(Track track) {
        synchronized (lock) {
            if (current == null) {
                startTrackLocked(track, false);
                return AddResult.STARTED_NOW;
            }
            if (queue.size() >= MAX_QUEUE_SIZE) {
                log.warn("Queue full in guild {} ({} tracks)", gm.getGuildId(), MAX_QUEUE_SIZE);
                return AddResult.REJECTED_FULL;
            }
            queue.add(track);
            log.debug("Enqueued '{}' in guild {} (position {})",
                    track.getInfo().getTitle(), gm.getGuildId(), queue.size());
            return AddResult.QUEUED;
        }
    }

    /** Inserts at the front of the queue; starts immediately if nothing is playing. */
    public AddResult enqueueNext(Track track) {
        synchronized (lock) {
            if (current == null) {
                startTrackLocked(track, false);
                return AddResult.STARTED_NOW;
            }
            if (queue.size() >= MAX_QUEUE_SIZE) {
                log.warn("Queue full in guild {} ({} tracks)", gm.getGuildId(), MAX_QUEUE_SIZE);
                return AddResult.REJECTED_FULL;
            }
            queue.add(0, track);
            log.debug("Inserted next '{}' in guild {}", track.getInfo().getTitle(), gm.getGuildId());
            return AddResult.QUEUED;
        }
    }

    public void enqueueAll(List<Track> tracks) {
        if (tracks.isEmpty()) {
            return;
        }
        synchronized (lock) {
            int space = MAX_QUEUE_SIZE - queue.size();
            List<Track> toAdd = tracks.size() <= space ? tracks : tracks.subList(0, Math.max(0, space));
            if (toAdd.isEmpty()) {
                log.warn("Queue full in guild {}, dropped {} tracks", gm.getGuildId(), tracks.size());
                return;
            }
            if (toAdd.size() < tracks.size()) {
                log.warn("Truncated bulk enqueue in guild {} to {} tracks (limit {})",
                        gm.getGuildId(), toAdd.size(), MAX_QUEUE_SIZE);
            }
            queue.addAll(toAdd);
            if (current == null) {
                playNextOrStopLocked(false);
            }
        }
    }

    public boolean shuffle() {
        synchronized (lock) {
            if (queue.size() < 2) {
                return false;
            }
            Collections.shuffle(queue);
            return true;
        }
    }

    public void clearQueue() {
        synchronized (lock) {
            queue.clear();
        }
    }

    public Track removeAt(int index) {
        synchronized (lock) {
            if (index >= 0 && index < queue.size()) {
                return queue.remove(index);
            }
            return null;
        }
    }

    public Track skip() {
        synchronized (lock) {
            Track cur = current;
            if (loopMode == LoopMode.QUEUE && cur != null) {
                queue.add(cur.makeClone());
            }
            return playNextOrStopLocked(true);
        }
    }

    public void stop() {
        synchronized (lock) {
            queue.clear();
            current = null;
        }
        gm.getLink().createOrUpdatePlayer()
                .setPaused(false)
                .setTrack(null)
                .subscribe();
        log.info("Stopped playback in guild {}", gm.getGuildId());
    }

    public void onTrackStart(Track track) {
        synchronized (lock) {
            current = track;
        }
    }

    public void onTrackEnd(Track lastTrack, AudioTrackEndReason endReason) {
        if (!endReason.getMayStartNext()) {
            log.debug("Track end in guild {} will not advance (reason {})", gm.getGuildId(), endReason);
            return;
        }

        synchronized (lock) {
            switch (loopMode) {
                case TRACK -> startTrackLocked(lastTrack.makeClone(), true);
                case QUEUE -> {
                    queue.add(lastTrack.makeClone());
                    playNextOrStopLocked(true);
                }
                case OFF -> playNextOrStopLocked(true);
            }
        }
    }

    private Track playNextOrStopLocked(boolean announce) {
        Track next = queue.isEmpty() ? null : queue.remove(0);
        if (next != null) {
            startTrackLocked(next, announce);
        } else {
            current = null;
            var link = gm.getCachedLink();
            if (link != null) {
                link.createOrUpdatePlayer().setTrack(null).subscribe();
            }
            log.debug("Queue drained in guild {}", gm.getGuildId());
        }
        return next;
    }

    private void startTrackLocked(Track track, boolean announce) {
        current = track;
        log.info("Starting '{}' in guild {}", track.getInfo().getTitle(), gm.getGuildId());
        gm.getLink().createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(volume)
                .setFilters(filter.build())
                .subscribe(
                        player -> {
                        },
                        error -> log.error("Failed to start track in guild {}", gm.getGuildId(), error)
                );
        if (announce) {
            announceNowPlaying(track);
        }
    }

    public void applyVolume(int newVolume) {
        volume = Math.max(0, Math.min(200, newVolume));
        var link = gm.getCachedLink();
        if (link != null) {
            link.createOrUpdatePlayer().setVolume(volume).subscribe();
        }
    }

    public void applyFilter(AudioFilter newFilter) {
        filter = newFilter;
        var link = gm.getCachedLink();
        if (link != null) {
            link.createOrUpdatePlayer().setFilters(newFilter.build()).subscribe();
        }
    }

    private void announceNowPlaying(Track track) {
        MessageChannel channel = resolveChannel(track);
        if (channel == null) {
            return;
        }
        var info = track.getInfo();
        var embed = Embeds.music()
                .setAuthor("Now playing")
                .setTitle(info.getTitle().isBlank() ? "Unknown" : info.getTitle(), info.getUri())
                .setDescription("by " + info.getAuthor())
                .build();
        boolean paused = gm.getPlayer() != null && gm.getPlayer().getPaused();
        channel.sendMessageEmbeds(embed)
                .setComponents(MusicControls.playbackRow(gm.getGuildId(), paused))
                .queue(null, failure -> log.warn("Failed to announce track in guild {}", gm.getGuildId(), failure));
    }

    private MessageChannel resolveChannel(Track track) {
        try {
            TrackUserData data = track.getUserData(TrackUserData.class);
            if (data == null || data.textChannelId() == 0L) {
                return null;
            }
            var jda = gm.getManager().getJda();
            if (jda == null) {
                return null;
            }
            return jda.getTextChannelById(data.textChannelId());
        } catch (Exception e) {
            log.debug("Could not resolve announce channel in guild {}", gm.getGuildId(), e);
            return null;
        }
    }
}
