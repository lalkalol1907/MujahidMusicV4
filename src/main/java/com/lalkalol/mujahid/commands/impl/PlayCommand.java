package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.Identifiers;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PlayCommand implements Command {
    @Override
    public String name() {
        return "play";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("play", "Play a track from a search query or URL.")
                .addOption(OptionType.STRING, "query", "A search term or a YouTube/SoundCloud/direct URL", true);
    }

    @Override
    public void execute(CommandContext ctx) {
        String query = ctx.getEvent().getOption("query").getAsString();
        PlaySupport.loadAndPlay(ctx, Identifiers.fromQuery(query));
    }
}
