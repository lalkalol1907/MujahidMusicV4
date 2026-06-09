package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ShuffleCommand implements Command {
    @Override
    public String name() {
        return "shuffle";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("shuffle", "Shuffle the queue.");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (ctx.getMusicManager().getScheduler().shuffle()) {
            ctx.reply("Shuffled the queue.");
        } else {
            ctx.replyEphemeral("Need at least 2 tracks in the queue to shuffle.");
        }
    }
}
