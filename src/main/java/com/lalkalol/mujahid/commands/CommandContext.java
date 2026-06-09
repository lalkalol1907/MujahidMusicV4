package com.lalkalol.mujahid.commands;

import com.lalkalol.mujahid.audio.GuildMusicManager;
import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.db.PlaylistRepository;
import com.lalkalol.mujahid.util.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandContext {
    private final SlashCommandInteractionEvent event;
    private final LavalinkManager lavalink;
    private final PlaylistRepository playlists;

    public CommandContext(
            SlashCommandInteractionEvent event,
            LavalinkManager lavalink,
            PlaylistRepository playlists
    ) {
        this.event = event;
        this.lavalink = lavalink;
        this.playlists = playlists;
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    public LavalinkManager getLavalink() {
        return lavalink;
    }

    public PlaylistRepository getPlaylists() {
        return playlists;
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public Member getMember() {
        return event.getMember();
    }

    public GuildMusicManager getMusicManager() {
        return lavalink.getOrCreate(getGuild().getIdLong());
    }

    public AudioChannel getMemberVoiceChannel() {
        var voiceState = getMember().getVoiceState();
        return voiceState != null ? voiceState.getChannel() : null;
    }

    public AudioChannel getSelfVoiceChannel() {
        var voiceState = getGuild().getSelfMember().getVoiceState();
        return voiceState != null ? voiceState.getChannel() : null;
    }

    public void reply(String message) {
        event.replyEmbeds(Embeds.success(message)).queue();
    }

    public void replyInfo(String message) {
        event.replyEmbeds(Embeds.info(message)).queue();
    }

    public void replyEphemeral(String message) {
        event.replyEmbeds(Embeds.warning(message)).setEphemeral(true).queue();
    }

    public void replyError(String message) {
        event.replyEmbeds(Embeds.error(message)).setEphemeral(true).queue();
    }

    public boolean ensureConnected() {
        AudioChannel memberChannel = getMemberVoiceChannel();
        if (memberChannel == null) {
            replyEphemeral("You need to be in a voice channel first.");
            return false;
        }
        AudioChannel self = getSelfVoiceChannel();
        if (self == null) {
            event.getJDA().getDirectAudioController().connect(memberChannel);
            lavalink.getOrCreate(getGuild().getIdLong());
            return true;
        }
        if (self.getIdLong() != memberChannel.getIdLong()) {
            replyEphemeral("You must be in my voice channel (" + self.getName() + ") to do that.");
            return false;
        }
        return true;
    }

    public boolean requireActivePlayer() {
        var player = getMusicManager().getPlayer();
        if (player == null || (player.getTrack() == null && getMusicManager().getScheduler().getCurrent() == null)) {
            replyEphemeral("Nothing is playing right now.");
            return false;
        }
        return true;
    }
}
