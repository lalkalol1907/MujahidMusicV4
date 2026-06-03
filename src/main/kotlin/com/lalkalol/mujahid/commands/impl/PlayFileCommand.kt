package com.lalkalol.mujahid.commands.impl

import com.lalkalol.mujahid.commands.Command
import com.lalkalol.mujahid.commands.CommandContext
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class PlayFileCommand : Command {
    override val name = "playfile"
    override val data = Commands.slash("playfile", "Play an uploaded audio file (mp3, ogg, wav, flac, m4a).")
        .addOption(OptionType.ATTACHMENT, "file", "The audio file to play", true)

    override fun execute(ctx: CommandContext) {
        val attachment = ctx.event.getOption("file")!!.asAttachment
        val contentType = attachment.contentType ?: ""
        if (!contentType.startsWith("audio")) {
            ctx.replyError(
                "`${attachment.fileName}` is not an audio file (detected `${contentType.ifBlank { "unknown" }}`).",
            )
            return
        }
        loadAndPlay(ctx, attachment.url)
    }
}
