package com.lalkalol.mujahid.listeners;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.commands.CommandRegistry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(InteractionListener.class);

    private final CommandRegistry registry;
    private final LavalinkManager lavalink;
    private final Long devGuildId;

    public InteractionListener(CommandRegistry registry, LavalinkManager lavalink, Long devGuildId) {
        this.registry = registry;
        this.lavalink = lavalink;
        this.devGuildId = devGuildId;
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.info("Logged in as {}", event.getJDA().getSelfUser().getAsTag());
        var commands = registry.commandData();

        if (devGuildId == null) {
            event.getJDA().updateCommands().addCommands(commands).queue(success ->
                    log.info("Registered {} commands globally", commands.size())
            );
            return;
        }

        Guild guild = event.getJDA().getGuildById(devGuildId);
        if (guild != null) {
            guild.updateCommands().addCommands(commands).queue(success ->
                    log.info("Registered {} commands to dev guild {}", commands.size(), devGuildId)
            );
            return;
        }
        log.warn("DEV_GUILD_ID {} not found; registering commands globally instead", devGuildId);
        event.getJDA().updateCommands().addCommands(commands).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        registry.handle(event);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();
        long selfId = guild.getSelfMember().getIdLong();

        if (event.getMember().getIdLong() == selfId) {
            if (event.getChannelJoined() == null) {
                lavalink.destroy(guild.getIdLong());
            }
            return;
        }

        AudioChannel selfChannel = guild.getSelfMember().getVoiceState().getChannel();
        if (selfChannel == null) {
            return;
        }
        if (event.getChannelLeft() == null || event.getChannelLeft().getIdLong() != selfChannel.getIdLong()) {
            return;
        }

        long remainingHumans = selfChannel.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .count();
        if (remainingHumans == 0) {
            log.info("Bot alone in '{}' (guild {}); leaving", selfChannel.getName(), guild.getIdLong());
            var existing = lavalink.getExisting(guild.getIdLong());
            if (existing != null) {
                existing.getScheduler().stop();
            }
            guild.getJDA().getDirectAudioController().disconnect(guild);
            lavalink.destroy(guild.getIdLong());
        }
    }
}
