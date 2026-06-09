package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.audio.AudioFilter;
import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class FilterCommand implements Command {
    @Override
    public String name() {
        return "filter";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("filter", "Apply an audio filter preset.")
                .addOptions(
                        new OptionData(OptionType.STRING, "preset", "Filter preset to apply", true)
                                .addChoice("Off", "off")
                                .addChoice("Bass Boost", "bassboost")
                                .addChoice("Nightcore", "nightcore")
                                .addChoice("8D", "8d")
                                .addChoice("Karaoke", "karaoke")
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        if (!ctx.requireActivePlayer()) {
            return;
        }
        AudioFilter preset = AudioFilter.from(ctx.getEvent().getOption("preset").getAsString());
        if (preset == null) {
            preset = AudioFilter.OFF;
        }
        ctx.getMusicManager().getScheduler().applyFilter(preset);
        if (preset == AudioFilter.OFF) {
            ctx.reply("Filters cleared.");
        } else {
            ctx.reply("Filter set to **" + preset.getDisplayName() + "**.");
        }
    }
}
