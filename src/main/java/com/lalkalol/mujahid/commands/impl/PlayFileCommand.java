package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PlayFileCommand implements Command {
    @Override
    public String name() {
        return "playfile";
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("playfile", "Play an uploaded audio file (mp3, ogg, wav, flac, m4a).")
                .addOption(OptionType.ATTACHMENT, "file", "The audio file to play", true);
    }

    @Override
    public void execute(CommandContext ctx) {
        var attachment = ctx.getEvent().getOption("file").getAsAttachment();
        String contentType = attachment.getContentType() != null ? attachment.getContentType() : "";
        if (!contentType.startsWith("audio")) {
            ctx.replyError(
                    "`" + attachment.getFileName() + "` is not an audio file (detected `"
                            + (contentType.isBlank() ? "unknown" : contentType) + "`)."
            );
            return;
        }
        PlaySupport.loadAndPlay(ctx, attachment.getUrl());
    }
}
