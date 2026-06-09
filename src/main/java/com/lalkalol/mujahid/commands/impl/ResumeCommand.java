package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ResumeCommand implements Command {
    @Override
    public String name() {
        return "resume";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("resume", "Resume playback.");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!ctx.requireActivePlayer()) {
            return;
        }
        var player = ctx.getMusicManager().getPlayer();
        if (player != null && !player.getPaused()) {
            ctx.replyEphemeral("Already playing.");
            return;
        }
        var link = ctx.getMusicManager().getCachedLink();
        if (link != null) {
            link.createOrUpdatePlayer().setPaused(false).subscribe();
        }
        ctx.reply("Resumed.");
    }
}
