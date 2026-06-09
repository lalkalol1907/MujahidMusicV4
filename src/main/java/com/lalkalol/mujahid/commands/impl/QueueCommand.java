package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.Embeds;
import com.lalkalol.mujahid.util.Format;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public class QueueCommand implements Command {
    private static final int QUEUE_PAGE_SIZE = 10;

    @Override
    public String name() {
        return "queue";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("queue", "Show the current queue.");
    }

    @Override
    public void execute(CommandContext ctx) {
        var scheduler = ctx.getMusicManager().getScheduler();
        Track current = scheduler.getCurrent();
        List<Track> queue = scheduler.queueSnapshot();

        if (current == null && queue.isEmpty()) {
            ctx.replyEphemeral("The queue is empty.");
            return;
        }

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
        if (!queue.isEmpty()) {
            description.append("**Up next:**\n");
            int limit = Math.min(queue.size(), QUEUE_PAGE_SIZE);
            for (int i = 0; i < limit; i++) {
                Track track = queue.get(i);
                description.append("`")
                        .append(i + 1)
                        .append(".` ")
                        .append(track.getInfo().getTitle())
                        .append(" `")
                        .append(Format.duration(track.getInfo().getLength(), track.getInfo().isStream()))
                        .append("`\n");
            }
            if (queue.size() > QUEUE_PAGE_SIZE) {
                description.append("\n…and **")
                        .append(queue.size() - QUEUE_PAGE_SIZE)
                        .append("** more.");
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

        ctx.getEvent().replyEmbeds(embed).queue();
    }
}
