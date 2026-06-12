package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.metrics.MetricsHolder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class LeaveCommand implements Command {
    @Override
    public String name() {
        return "leave";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("leave", "Leave the voice channel and clear the queue.");
    }

    @Override
    public void execute(CommandContext ctx) {
        if (ctx.getSelfVoiceChannel() == null) {
            ctx.replyEphemeral("I'm not connected to a voice channel.");
            return;
        }
        var existing = ctx.getLavalink().getExisting(ctx.getGuild().getIdLong());
        if (existing != null) {
            existing.getScheduler().stop();
        }
        ctx.getEvent().getJDA().getDirectAudioController().disconnect(ctx.getGuild());
        ctx.getLavalink().destroy(ctx.getGuild().getIdLong());
        MetricsHolder.get().recordVoiceSession("leave");
        MetricsHolder.get().setActivePlayers(ctx.getLavalink().activePlayerCount());
        ctx.reply("Left the voice channel. Bye!");
    }
}
