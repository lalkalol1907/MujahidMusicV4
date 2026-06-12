package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.metrics.MetricsHolder;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class JoinCommand implements Command {
    @Override
    public String name() {
        return "join";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("join", "Join your current voice channel.");
    }

    @Override
    public void execute(CommandContext ctx) {
        AudioChannel channel = ctx.getMemberVoiceChannel();
        if (channel == null) {
            ctx.replyEphemeral("You need to be in a voice channel first.");
            return;
        }
        ctx.getEvent().getJDA().getDirectAudioController().connect(channel);
        ctx.getLavalink().getOrCreate(ctx.getGuild().getIdLong());
        MetricsHolder.get().recordVoiceSession("join");
        MetricsHolder.get().setActivePlayers(ctx.getLavalink().activePlayerCount());
        ctx.reply("Joined **" + channel.getName() + "**.");
    }
}
