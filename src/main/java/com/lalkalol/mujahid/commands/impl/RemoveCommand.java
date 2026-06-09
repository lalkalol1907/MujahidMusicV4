package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class RemoveCommand implements Command {
    @Override
    public String name() {
        return "remove";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("remove", "Remove a track from the queue by position.")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "position", "Queue position (see /queue)", true)
                                .setMinValue(1)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        int position = ctx.getEvent().getOption("position").getAsInt();
        Track removed = ctx.getMusicManager().getScheduler().removeAt(position - 1);
        if (removed == null) {
            ctx.replyEphemeral("There is no track at position " + position + ".");
        } else {
            ctx.reply("Removed **" + (removed.getInfo().getTitle().isBlank() ? "Unknown" : removed.getInfo().getTitle())
                    + "** from the queue.");
        }
    }
}
