package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.Format;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SeekCommand implements Command {
    @Override
    public String name() {
        return "seek";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("seek", "Seek to a position in the current track.")
                .addOption(OptionType.STRING, "position", "Timestamp like 90, 1:30 or 1:02:03", true);
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
        if (track.getInfo().isStream() || !track.getInfo().isSeekable()) {
            ctx.replyEphemeral("This track cannot be seeked.");
            return;
        }

        String raw = ctx.getEvent().getOption("position").getAsString();
        Long ms = Format.parseTimestamp(raw);
        if (ms == null) {
            ctx.replyEphemeral("Invalid timestamp. Use `90`, `1:30` or `1:02:03`.");
            return;
        }
        if (ms > track.getInfo().getLength()) {
            ctx.replyEphemeral("That position is past the end of the track ("
                    + Format.duration(track.getInfo().getLength()) + ").");
            return;
        }

        var link = ctx.getMusicManager().getCachedLink();
        if (link != null) {
            link.createOrUpdatePlayer().setPosition(ms).subscribe();
        }
        ctx.reply("Seeked to **" + Format.duration(ms) + "**.");
    }
}
