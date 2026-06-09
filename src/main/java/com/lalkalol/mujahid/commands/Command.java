package com.lalkalol.mujahid.commands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface Command {
    String name();

    SlashCommandData data();

    void execute(CommandContext ctx);
}
