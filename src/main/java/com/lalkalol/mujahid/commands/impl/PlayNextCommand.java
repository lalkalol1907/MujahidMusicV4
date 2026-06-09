package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.Identifiers;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PlayNextCommand implements Command {
    @Override
    public String name() {
        return "playnext";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("playnext", "Play a track next in the queue (after the current song).")
                .addOption(OptionType.STRING, "query", "A search term or a YouTube/SoundCloud/direct URL", true);
    }

    @Override
    public void execute(CommandContext ctx) {
        String query = ctx.getEvent().getOption("query").getAsString();
        PlaySupport.loadAndEnqueueNext(ctx, Identifiers.fromQuery(query));
    }
}
