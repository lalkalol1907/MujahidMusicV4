package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import com.lalkalol.mujahid.util.QueuePages
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class QueueCommand : Command {
    override fun name(): String = "queue"

    override fun data(): SlashCommandData =
        Commands.slash("queue", "Show the current queue.")
            .addOption(OptionType.INTEGER, "page", "Page number", false)

    override suspend fun execute(ctx: CommandContext) {
        val scheduler = ctx.getMusicManager().scheduler
        if (scheduler.getCurrent() == null && scheduler.queueSize() == 0) {
            ctx.replyEphemeral("The queue is empty.")
            return
        }

        var page = 1
        ctx.event.getOption("page")?.let { page = it.asInt }

        val view = QueuePages.build(ctx.getGuild().idLong, scheduler, page)
        val reply = ctx.event.replyEmbeds(view.embed)
        if (view.components.isNotEmpty()) {
            reply.setComponents(view.components)
        }
        reply.queue()
    }
}
