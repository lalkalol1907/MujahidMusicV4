package com.lalkalol.mujahid.audio;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

public class GuildMusicManager {
    private final long guildId;
    private final LavalinkManager manager;
    private final TrackScheduler scheduler;

    public GuildMusicManager(long guildId, LavalinkManager manager) {
        this.guildId = guildId;
        this.manager = manager;
        this.scheduler = new TrackScheduler(this);
    }

    public long getGuildId() {
        return guildId;
    }

    public LavalinkManager getManager() {
        return manager;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public Link getLink() {
        return manager.getClient().getOrCreateLink(guildId);
    }

    public Link getCachedLink() {
        return manager.getClient().getLinkIfCached(guildId);
    }

    public LavalinkPlayer getPlayer() {
        Link link = getCachedLink();
        return link != null ? link.getCachedPlayer() : null;
    }
}
