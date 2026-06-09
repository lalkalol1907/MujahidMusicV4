package com.lalkalol.mujahid.audio;

import com.lalkalol.mujahid.util.Embeds;
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

    public boolean enqueue(Track track) {
        if (current == null) {
            startTrack(track);
            return true;
        }
        synchronized (lock) {
            queue.add(track);
        }
        return false;
    }

    public void enqueueAll(List<Track> tracks) {
        if (tracks.isEmpty()) {
            return;
        }
        synchronized (lock) {
            queue.addAll(tracks);
        }
        if (current == null) {
            playNextOrStop();
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
        Track cur = current;
        if (loopMode == LoopMode.QUEUE && cur != null) {
            synchronized (lock) {
                queue.add(cur.makeClone());
            }
        }
        return playNextOrStop();
    }

    public void stop() {
        clearQueue();
        current = null;
        gm.getLink().createOrUpdatePlayer()
                .setPaused(false)
                .setTrack(null)
                .subscribe();
    }

    public void onTrackStart(Track track) {
        current = track;
        announceNowPlaying(track);
    }

    public void onTrackEnd(Track lastTrack, AudioTrackEndReason endReason) {
        if (!endReason.getMayStartNext()) {
            return;
        }

        switch (loopMode) {
            case TRACK -> startTrack(lastTrack.makeClone());
            case QUEUE -> {
                synchronized (lock) {
                    queue.add(lastTrack.makeClone());
                }
                playNextOrStop();
            }
            case OFF -> playNextOrStop();
        }
    }

    private Track playNextOrStop() {
        Track next;
        synchronized (lock) {
            next = queue.isEmpty() ? null : queue.remove(0);
        }
        if (next != null) {
            startTrack(next);
        } else {
            current = null;
            var link = gm.getCachedLink();
            if (link != null) {
                link.createOrUpdatePlayer().setTrack(null).subscribe();
            }
        }
        return next;
    }

    private void startTrack(Track track) {
        current = track;
        gm.getLink().createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(volume)
                .setFilters(filter.build())
                .subscribe(
                        player -> {
                        },
                        error -> log.error("Failed to start track in guild {}", gm.getGuildId(), error)
                );
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
        channel.sendMessageEmbeds(embed).queue(null, failure -> {
        });
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
        } catch (Exception ignored) {
            return null;
        }
    }
}
