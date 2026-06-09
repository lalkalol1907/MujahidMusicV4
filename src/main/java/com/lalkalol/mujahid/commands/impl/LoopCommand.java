package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.audio.LoopMode;
import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class LoopCommand implements Command {
    @Override
    public String name() {
        return "loop";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("loop", "Set the loop mode.")
                .addOptions(
                        new OptionData(OptionType.STRING, "mode", "What to loop", true)
                                .addChoice("Off", "off")
                                .addChoice("Track", "track")
                                .addChoice("Queue", "queue")
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        LoopMode mode = LoopMode.from(ctx.getEvent().getOption("mode").getAsString());
        if (mode == null) {
            mode = LoopMode.OFF;
        }
        ctx.getMusicManager().getScheduler().setLoopMode(mode);
        ctx.reply("Loop mode set to **" + mode.name().toLowerCase() + "**.");
    }
}
