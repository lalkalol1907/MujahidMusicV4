package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.Embeds;
import com.lalkalol.mujahid.util.Format;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class NowPlayingCommand implements Command {
    @Override
    public String name() {
        return "nowplaying";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("nowplaying", "Show what is currently playing.");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!ctx.requireActivePlayer()) {
            return;
        }
        var player = ctx.getMusicManager().getPlayer();
        Track track = player != null ? player.getTrack() : ctx.getMusicManager().getScheduler().getCurrent();
        if (track == null) {
            ctx.replyEphemeral("Nothing is playing right now.");
            return;
        }

        var info = track.getInfo();
        long position = player != null ? player.getPosition() : 0L;
        String bar = Format.progressBar(position, info.getLength());
        String timeline = Format.duration(position) + " / " + Format.duration(info.getLength(), info.isStream());

        var scheduler = ctx.getMusicManager().getScheduler();
        String state = player != null && player.getPaused() ? "Paused" : "Playing";

        var embed = Embeds.music()
                .setAuthor("Now playing")
                .setTitle(info.getTitle().isBlank() ? "Unknown" : info.getTitle(), info.getUri())
                .setDescription("by " + info.getAuthor() + "\n\n" + bar + "\n`" + timeline + "`")
                .setFooter(state + " • Loop: " + scheduler.getLoopMode()
                        + " • Filter: " + scheduler.getFilter().getDisplayName())
                .build();

        ctx.getEvent().replyEmbeds(embed).queue();
    }
}
