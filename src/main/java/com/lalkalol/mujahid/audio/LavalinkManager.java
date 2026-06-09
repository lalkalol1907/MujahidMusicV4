package com.lalkalol.mujahid.audio;

import com.lalkalol.mujahid.config.Config;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.client.event.StatsEvent;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import dev.arbjerg.lavalink.client.Helpers;

public class LavalinkManager {
    private static final int SESSION_INVALID = 4006;
    private static final Logger log = LoggerFactory.getLogger(LavalinkManager.class);

    private final Config config;
    private final LavalinkClient client;
    private final ConcurrentHashMap<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    private volatile JDA jda;

    public LavalinkManager(Config config) {
        this.config = config;
        this.client = new LavalinkClient(Helpers.getUserIdFromToken(config.discordToken()));
    }

    public LavalinkClient getClient() {
        return client;
    }

    public JDA getJda() {
        return jda;
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public void start() {
        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        registerEventListeners();
        addNode();
    }

    public GuildMusicManager getOrCreate(long guildId) {
        return musicManagers.computeIfAbsent(guildId, id -> new GuildMusicManager(id, this));
    }

    public GuildMusicManager getExisting(long guildId) {
        return musicManagers.get(guildId);
    }

    public void destroy(long guildId) {
        musicManagers.remove(guildId);
        log.debug("Destroyed music manager for guild {}", guildId);
    }

    public void shutdown() {
        log.info("Shutting down Lavalink client");
        client.close();
    }

    private void addNode() {
        client.addNode(
                new NodeOptions.Builder()
                        .setName("main")
                        .setServerUri(config.lavalinkUri())
                        .setPassword(config.lavalinkPassword())
                        .build()
        );
    }

    private void registerEventListeners() {
        client.on(ReadyEvent.class).subscribe(event ->
                log.info("Lavalink node '{}' ready (session {})", event.getNode().getName(), event.getSessionId())
        );

        client.on(StatsEvent.class).subscribe(event ->
                log.debug(
                        "Node '{}' players {}/{}",
                        event.getNode().getName(),
                        event.getPlayingPlayers(),
                        event.getPlayers()
                )
        );

        client.on(TrackStartEvent.class).subscribe(event -> {
            GuildMusicManager manager = getExisting(event.getGuildId());
            if (manager != null) {
                manager.getScheduler().onTrackStart(event.getTrack());
            }
        });

        client.on(TrackEndEvent.class).subscribe(event -> {
            GuildMusicManager manager = getExisting(event.getGuildId());
            if (manager != null) {
                manager.getScheduler().onTrackEnd(event.getTrack(), event.getEndReason());
            }
        });

        client.on(WebSocketClosedEvent.class).subscribe(event -> {
            if (event.getCode() == SESSION_INVALID) {
                reconnectVoice(event.getGuildId());
            }
        });
    }

    private void reconnectVoice(long guildId) {
        if (jda == null) {
            return;
        }
        var guild = jda.getGuildById(guildId);
        if (guild == null) {
            return;
        }
        var channel = guild.getSelfMember().getVoiceState().getChannel();
        if (channel == null) {
            return;
        }
        log.warn("Voice session invalid for guild {}, reconnecting", guildId);
        guild.getJDA().getDirectAudioController().reconnect(channel);
    }
}
