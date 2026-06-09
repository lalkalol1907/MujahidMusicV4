package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class VolumeCommand implements Command {
    @Override
    public String name() {
        return "volume";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("volume", "Set the playback volume (0-200).")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "level", "Volume percentage from 0 to 200", true)
                                .setRequiredRange(0, 200)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!ctx.requireActivePlayer()) {
            return;
        }
        int level = Math.max(0, Math.min(200, ctx.getEvent().getOption("level").getAsInt()));
        ctx.getMusicManager().getScheduler().applyVolume(level);
        ctx.reply("Volume set to **" + level + "%**.");
    }
}
