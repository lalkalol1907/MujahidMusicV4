package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class StopCommand implements Command {
    @Override
    public String name() {
        return "stop";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("stop", "Stop playback and clear the queue.");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!ctx.requireActivePlayer()) {
            return;
        }
        ctx.getMusicManager().getScheduler().stop();
        ctx.reply("Stopped playback and cleared the queue.");
    }
}
