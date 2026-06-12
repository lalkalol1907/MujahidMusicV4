package com.lalkalol.mujahid.internal;

import com.lalkalol.mujahid.audio.GuildMusicManager;
import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.internal.dto.NodeDto;
import com.lalkalol.mujahid.internal.dto.QueueTrackDto;
import com.lalkalol.mujahid.internal.dto.SessionDto;
import com.lalkalol.mujahid.internal.dto.SessionsResponse;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LavalinkStateService {

    private static final Logger log = LoggerFactory.getLogger(LavalinkStateService.class);

    private final LavalinkManager lavalinkManager;

    public LavalinkStateService(LavalinkManager lavalinkManager) {
        this.lavalinkManager = lavalinkManager;
    }

    public SessionsResponse getSessions() {
        try {
            List<LavalinkNode> nodes = lavalinkManager.getClient().getNodes();
            List<NodeDto> nodeDtos = new ArrayList<>();
            for (LavalinkNode node : nodes) {
                var stats = node.getStats();
                nodeDtos.add(new NodeDto(
                        node.getName(),
                        node.getBaseUri(),
                        node.getAvailable(),
                        node.getSessionId(),
                        stats != null ? stats.getPlayers() : 0,
                        stats != null ? stats.getPlayingPlayers() : 0
                ));
            }

            JDA jda = lavalinkManager.getJda();
            List<SessionDto> sessions = new ArrayList<>();

            for (LavalinkNode node : nodes) {
                for (Map.Entry<Long, GuildMusicManager> entry : lavalinkManager.getAllManagers()) {
                    long guildId = entry.getKey();
                    GuildMusicManager gm = entry.getValue();

                    var link = gm.getCachedLink();
                    if (link == null) {
                        continue;
                    }

                    String nodeName;
                    try {
                        nodeName = link.getNode().getName();
                    } catch (Exception e) {
                        nodeName = "unknown";
                    }
                    if (!nodeName.equals(node.getName())) {
                        continue;
                    }

                    var scheduler = gm.getScheduler();
                    var current = scheduler.getCurrent();
                    LavalinkPlayer player = gm.getPlayer();

                    String guildName = String.valueOf(guildId);
                    if (jda != null) {
                        Guild guild = jda.getGuildById(guildId);
                        if (guild != null) {
                            guildName = guild.getName();
                        }
                    }

                    String trackTitle = null;
                    String trackAuthor = null;
                    long lengthMs = 0;
                    if (current != null) {
                        var info = current.getInfo();
                        trackTitle = info.getTitle();
                        trackAuthor = info.getAuthor();
                        lengthMs = info.getLength();
                    }

                    long positionMs = 0;
                    boolean paused = false;
                    int volume = 100;
                    if (player != null) {
                        positionMs = player.getPosition();
                        paused = player.getPaused();
                        volume = player.getVolume();
                    }

                    sessions.add(new SessionDto(
                            String.valueOf(guildId),
                            guildName,
                            nodeName,
                            trackTitle,
                            trackAuthor,
                            positionMs,
                            lengthMs,
                            paused,
                            volume,
                            scheduler.queueSize(),
                            scheduler.getLoopMode().name().toLowerCase()
                    ));
                }
            }

            return new SessionsResponse(nodeDtos, sessions);
        } catch (Exception e) {
            log.error("Failed to build sessions response", e);
            return new SessionsResponse(List.of(), List.of());
        }
    }

    public List<QueueTrackDto> getQueue(long guildId) {
        GuildMusicManager gm = lavalinkManager.getExisting(guildId);
        if (gm == null) {
            return null;
        }

        List<QueueTrackDto> tracks = new ArrayList<>();
        Track current = gm.getScheduler().getCurrent();
        if (current != null) {
            var info = current.getInfo();
            tracks.add(new QueueTrackDto(
                    0,
                    blankToUnknown(info.getTitle()),
                    info.getAuthor(),
                    info.getUri(),
                    info.getLength()
            ));
        }

        int position = 1;
        for (Track track : gm.getScheduler().queueSnapshot()) {
            var info = track.getInfo();
            tracks.add(new QueueTrackDto(
                    position++,
                    blankToUnknown(info.getTitle()),
                    info.getAuthor(),
                    info.getUri(),
                    info.getLength()
            ));
        }
        return tracks;
    }

    private static String blankToUnknown(String title) {
        return title == null || title.isBlank() ? "Unknown" : title;
    }
}
