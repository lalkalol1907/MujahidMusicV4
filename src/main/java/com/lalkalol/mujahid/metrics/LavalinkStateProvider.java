package com.lalkalol.mujahid.metrics;

import com.lalkalol.mujahid.audio.GuildMusicManager;
import com.lalkalol.mujahid.audio.LavalinkManager;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the JSON response for GET /internal/lavalink-state.
 * Merges data from LavalinkClient (node stats) and GuildMusicManagers (guild info, queue, tracks).
 */
public class LavalinkStateProvider {

    private static final Logger log = LoggerFactory.getLogger(LavalinkStateProvider.class);

    private final LavalinkManager lavalinkManager;

    public LavalinkStateProvider(LavalinkManager lavalinkManager) {
        this.lavalinkManager = lavalinkManager;
    }

    /** Serialize current live state to a JSON string (no external library needed — hand-rolled). */
    public String toJson() {
        try {
            List<LavalinkNode> nodes = lavalinkManager.getClient().getNodes();
            StringBuilder sb = new StringBuilder("{");

            // nodes array
            sb.append("\"nodes\":[");
            for (int i = 0; i < nodes.size(); i++) {
                LavalinkNode node = nodes.get(i);
                boolean connected = node.getAvailable();
                var stats = node.getStats();   // Stats? — updated from getCachedStats()

                sb.append("{");
                sb.append("\"name\":").append(jsonStr(node.getName())).append(",");
                sb.append("\"uri\":").append(jsonStr(node.getBaseUri())).append(",");
                sb.append("\"connected\":").append(connected).append(",");
                sb.append("\"sessionId\":").append(jsonStr(node.getSessionId())).append(",");
                sb.append("\"players\":").append(stats != null ? stats.getPlayers() : 0).append(",");
                sb.append("\"playing\":").append(stats != null ? stats.getPlayingPlayers() : 0);
                sb.append("}");
                if (i < nodes.size() - 1) sb.append(",");
            }
            sb.append("],");

            // sessions array — one per active guild manager
            JDA jda = lavalinkManager.getJda();
            sb.append("\"sessions\":[");
            List<String> sessionEntries = new ArrayList<>();

            for (LavalinkNode node : nodes) {
                // Iterate all GuildMusicManagers to find sessions on this node
                List<Map.Entry<Long, GuildMusicManager>> managers = lavalinkManager.getAllManagers();
                for (Map.Entry<Long, GuildMusicManager> entry : managers) {
                    long guildId = entry.getKey();
                    GuildMusicManager gm = entry.getValue();

                    // Check if this manager's link is on this node
                    var link = gm.getCachedLink();
                    if (link == null) continue;

                    // Find which node this link belongs to
                    String nodeName = "unknown";
                    try {
                        nodeName = link.getNode().getName();
                    } catch (Exception ignored) {}

                    if (!nodeName.equals(node.getName())) continue;

                    var scheduler = gm.getScheduler();
                    var current = scheduler.getCurrent();
                    LavalinkPlayer player = gm.getPlayer();

                    String guildName = guildId + "";
                    if (jda != null) {
                        Guild guild = jda.getGuildById(guildId);
                        if (guild != null) {
                            guildName = guild.getName();
                        }
                    }

                    String trackTitle = null, trackAuthor = null;
                    long positionMs = 0, lengthMs = 0;
                    boolean paused = false;
                    int volume = 100;
                    int queueSize = scheduler.queueSize();
                    String loopMode = scheduler.getLoopMode().name().toLowerCase();

                    if (current != null) {
                        var info = current.getInfo();
                        trackTitle = info.getTitle();
                        trackAuthor = info.getAuthor();
                        lengthMs = info.getLength();
                    }
                    if (player != null) {
                        positionMs = player.getPosition();
                        paused = player.getPaused();
                        volume = player.getVolume();
                    }

                    StringBuilder session = new StringBuilder("{");
                    session.append("\"guildId\":").append(jsonStr(String.valueOf(guildId))).append(",");
                    session.append("\"guildName\":").append(jsonStr(guildName)).append(",");
                    session.append("\"nodeName\":").append(jsonStr(nodeName)).append(",");
                    session.append("\"trackTitle\":").append(trackTitle != null ? jsonStr(trackTitle) : "null").append(",");
                    session.append("\"trackAuthor\":").append(trackAuthor != null ? jsonStr(trackAuthor) : "null").append(",");
                    session.append("\"positionMs\":").append(positionMs).append(",");
                    session.append("\"lengthMs\":").append(lengthMs).append(",");
                    session.append("\"paused\":").append(paused).append(",");
                    session.append("\"volume\":").append(volume).append(",");
                    session.append("\"queueSize\":").append(queueSize).append(",");
                    session.append("\"loopMode\":").append(jsonStr(loopMode));
                    session.append("}");
                    sessionEntries.add(session.toString());
                }
            }

            sb.append(String.join(",", sessionEntries));
            sb.append("]}");
            return sb.toString();

        } catch (Exception e) {
            log.error("Failed to build lavalink state", e);
            return "{\"nodes\":[],\"sessions\":[]}";
        }
    }

    private static String jsonStr(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}
