package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ClearCommand implements Command {
    @Override
    public String name() {
        return "clear";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("clear", "Clear the queue without stopping the current track.");
    }

    @Override
    public void execute(CommandContext ctx) {
        int size = ctx.getMusicManager().getScheduler().queueSize();
        if (size == 0) {
            ctx.replyEphemeral("The queue is already empty.");
            return;
        }
        ctx.getMusicManager().getScheduler().clearQueue();
        ctx.reply("Cleared **" + size + "** track(s) from the queue.");
    }
}
