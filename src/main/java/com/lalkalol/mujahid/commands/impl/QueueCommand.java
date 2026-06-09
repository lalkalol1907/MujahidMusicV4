package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.QueuePages;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class QueueCommand implements Command {
    @Override
    public String name() {
        return "queue";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("queue", "Show the current queue.")
                .addOption(OptionType.INTEGER, "page", "Page number", false);
    }

    @Override
    public void execute(CommandContext ctx) {
        var scheduler = ctx.getMusicManager().getScheduler();
        if (scheduler.getCurrent() == null && scheduler.queueSize() == 0) {
            ctx.replyEphemeral("The queue is empty.");
            return;
        }

        int page = 1;
        if (ctx.getEvent().getOption("page") != null) {
            page = ctx.getEvent().getOption("page").getAsInt();
        }

        var view = QueuePages.build(ctx.getGuild().getIdLong(), scheduler, page);
        var reply = ctx.getEvent().replyEmbeds(view.embed());
        if (!view.components().isEmpty()) {
            reply.setComponents(view.components());
        }
        reply.queue();
    }
}
