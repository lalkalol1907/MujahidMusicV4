package com.lalkalol.mujahid.listeners;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.util.Embeds;
import com.lalkalol.mujahid.util.MusicControls;
import com.lalkalol.mujahid.util.QueuePages;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MusicButtonHandler {
    private static final Logger log = LoggerFactory.getLogger(MusicButtonHandler.class);

    private MusicButtonHandler() {
    }

    public static void handle(ButtonInteractionEvent event, LavalinkManager lavalink) {
        String id = event.getComponentId();
        if (!id.startsWith(MusicControls.PREFIX)) {
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Embeds.warning("This button only works in a server."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!canControl(event, guild)) {
            event.replyEmbeds(Embeds.warning("Join my voice channel to use these controls."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        long guildId = guild.getIdLong();
        var manager = lavalink.getOrCreate(guildId);
        var scheduler = manager.getScheduler();

        String[] parts = id.substring(MusicControls.PREFIX.length()).split(":");
        String action = parts[0];

        switch (action) {
            case "skip" -> {
                if (scheduler.getCurrent() == null && scheduler.queueSize() == 0) {
                    event.replyEmbeds(Embeds.warning("Nothing is playing.")).setEphemeral(true).queue();
                    return;
                }
                Track next = scheduler.skip();
                log.info("Button skip in guild {} by {}", guildId, event.getUser().getId());
                if (next == null) {
                    event.replyEmbeds(Embeds.success("Skipped. Queue is empty.")).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(Embeds.success("Skipped. Now playing **" + next.getInfo().getTitle() + "**."))
                            .setEphemeral(true)
                            .queue();
                }
            }
            case "pause" -> {
                var link = manager.getCachedLink();
                if (link != null) {
                    link.createOrUpdatePlayer().setPaused(true).subscribe();
                }
                log.debug("Button pause in guild {}", guildId);
                event.replyEmbeds(Embeds.success("Paused.")).setEphemeral(true).queue();
            }
            case "resume" -> {
                var link = manager.getCachedLink();
                if (link != null) {
                    link.createOrUpdatePlayer().setPaused(false).subscribe();
                }
                log.debug("Button resume in guild {}", guildId);
                event.replyEmbeds(Embeds.success("Resumed.")).setEphemeral(true).queue();
            }
            case "stop" -> {
                scheduler.stop();
                log.info("Button stop in guild {} by {}", guildId, event.getUser().getId());
                event.replyEmbeds(Embeds.success("Stopped and cleared the queue.")).setEphemeral(true).queue();
            }
            case "qprev", "qnext" -> {
                if (parts.length < 3) {
                    return;
                }
                int currentPage = Integer.parseInt(parts[2]);
                int targetPage = "qprev".equals(action) ? currentPage - 1 : currentPage + 1;
                var view = QueuePages.build(guildId, scheduler, targetPage);
                event.editMessageEmbeds(view.embed())
                        .setComponents(view.components())
                        .queue();
            }
            default -> {
            }
        }
    }

    private static boolean canControl(ButtonInteractionEvent event, Guild guild) {
        AudioChannel self = guild.getSelfMember().getVoiceState().getChannel();
        if (self == null) {
            return false;
        }
        if (event.getMember() == null || event.getMember().getVoiceState() == null) {
            return false;
        }
        AudioChannel member = event.getMember().getVoiceState().getChannel();
        return member != null && member.getIdLong() == self.getIdLong();
    }
}
