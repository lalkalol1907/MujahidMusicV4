package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SkipCommand implements Command {
    @Override
    public String name() {
        return "skip";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("skip", "Skip the current track.");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!ctx.ensureConnected()) {
            return;
        }
        if (!ctx.requireActivePlayer()) {
            return;
        }
        Track next = ctx.getMusicManager().getScheduler().skip();
        if (next == null) {
            ctx.reply("Skipped. The queue is now empty.");
        } else {
            ctx.reply("Skipped. Now playing **"
                    + (next.getInfo().getTitle().isBlank() ? "Unknown" : next.getInfo().getTitle()) + "**.");
        }
    }
}
