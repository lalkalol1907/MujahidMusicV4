package com.lalkalol.mujahid.util;

import com.lalkalol.mujahid.audio.TrackScheduler;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.components.actionrow.ActionRow;

import java.util.List;

public final class QueuePages {
    public static final int PAGE_SIZE = 10;

    private QueuePages() {
    }

    public record PageView(MessageEmbed embed, List<ActionRow> components, int page, int totalPages) {
    }

    public static int totalPages(int queueSize) {
        if (queueSize <= 0) {
            return 1;
        }
        return (queueSize + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public static int clampPage(int page, int totalPages) {
        return Math.max(1, Math.min(page, totalPages));
    }

    public static PageView build(long guildId, TrackScheduler scheduler, int requestedPage) {
        Track current = scheduler.getCurrent();
        List<Track> queue = scheduler.queueSnapshot();
        int totalPages = totalPages(queue.size());
        int page = clampPage(requestedPage, totalPages);

        StringBuilder description = new StringBuilder();
        if (current != null) {
            description.append("**Now playing:** [")
                    .append(current.getInfo().getTitle())
                    .append("](")
                    .append(current.getInfo().getUri())
                    .append(") `")
                    .append(Format.duration(current.getInfo().getLength(), current.getInfo().isStream()))
                    .append("`\n\n");
        }

        if (queue.isEmpty()) {
            description.append("*No tracks queued.*");
        } else {
            description.append("**Up next** (page ").append(page).append("/").append(totalPages).append("):\n");
            int start = (page - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, queue.size());
            for (int i = start; i < end; i++) {
                Track track = queue.get(i);
                description.append("`")
                        .append(i + 1)
                        .append(".` ")
                        .append(track.getInfo().getTitle())
                        .append(" `")
                        .append(Format.duration(track.getInfo().getLength(), track.getInfo().isStream()))
                        .append("`\n");
            }
        }

        var embed = Embeds.music()
                .setTitle("🎶 Queue")
                .setDescription(description.toString())
                .setFooter(
                        "Loop: " + scheduler.getLoopMode()
                                + " • Filter: " + scheduler.getFilter().getDisplayName()
                                + " • Volume: " + scheduler.getVolume() + "% • "
                                + queue.size() + " in queue"
                )
                .build();

        List<ActionRow> components = queue.isEmpty()
                ? List.of()
                : MusicControls.queueNavRow(guildId, page, totalPages);

        return new PageView(embed, components, page, totalPages);
    }
}
