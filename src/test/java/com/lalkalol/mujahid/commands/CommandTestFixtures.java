package com.lalkalol.mujahid.commands;

import com.lalkalol.mujahid.audio.GuildMusicManager;
import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.audio.TrackScheduler;
import com.lalkalol.mujahid.db.PlaylistRepository;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfMember;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CommandTestFixtures {
    private CommandTestFixtures() {
    }

    public static final long GUILD_ID = 42L;
    public static final long USER_ID = 100L;
    public static final long CHANNEL_ID = 200L;

    public static final class Harness {
        public final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);
        public final LavalinkManager lavalink = mock(LavalinkManager.class);
        public final PlaylistRepository playlists = mock(PlaylistRepository.class);
        public final GuildMusicManager musicManager = mock(GuildMusicManager.class);
        public final TrackScheduler scheduler = mock(TrackScheduler.class);
        public final Guild guild = mock(Guild.class);
        public final Member member = mock(Member.class);
        public final SelfMember selfMember = mock(SelfMember.class);
        public final User user = mock(User.class);
        public final JDA jda = mock(JDA.class);
        public final DirectAudioController audioController = mock(DirectAudioController.class);
        public final ReplyCallbackAction replyAction = mock(ReplyCallbackAction.class);

        private final List<MessageEmbed> replies = new ArrayList<>();
        private final List<MessageEmbed> ephemeralReplies = new ArrayList<>();

        Harness() {
            when(event.getGuild()).thenReturn(guild);
            when(event.getMember()).thenReturn(member);
            when(event.getUser()).thenReturn(user);
            when(event.getJDA()).thenReturn(jda);
            when(event.getChannelIdLong()).thenReturn(CHANNEL_ID);
            when(user.getIdLong()).thenReturn(USER_ID);
            when(jda.getDirectAudioController()).thenReturn(audioController);
            when(guild.getIdLong()).thenReturn(GUILD_ID);
            when(guild.getSelfMember()).thenReturn(selfMember);
            when(lavalink.getOrCreate(GUILD_ID)).thenReturn(musicManager);
            when(musicManager.getScheduler()).thenReturn(scheduler);

            when(event.replyEmbeds(any(MessageEmbed.class), any(MessageEmbed[].class))).thenAnswer(invocation -> {
                replies.add(invocation.getArgument(0));
                return replyAction;
            });
            when(replyAction.setEphemeral(true)).thenAnswer(invocation -> {
                if (!replies.isEmpty()) {
                    ephemeralReplies.add(replies.get(replies.size() - 1));
                }
                return replyAction;
            });
            doNothing().when(replyAction).queue();
            doNothing().when(replyAction).queue(any());
        }

        public CommandContext context() {
            return new CommandContext(event, lavalink, playlists);
        }

        public String lastReplyDescription() {
            return replies.get(replies.size() - 1).getDescription();
        }

        public String lastEphemeralDescription() {
            return ephemeralReplies.get(ephemeralReplies.size() - 1).getDescription();
        }

        public int replyCount() {
            return replies.size();
        }

        public Harness memberNotInVoice() {
            when(member.getVoiceState()).thenReturn(null);
            return this;
        }

        public Harness memberInVoice(long channelId, String name) {
            var voiceState = mock(net.dv8tion.jda.api.entities.GuildVoiceState.class);
            var channel = voiceChannel(channelId, name);
            when(member.getVoiceState()).thenReturn(voiceState);
            when(voiceState.getChannel()).thenReturn(channel);
            return this;
        }

        public Harness botNotInVoice() {
            when(selfMember.getVoiceState()).thenReturn(null);
            return this;
        }

        public Harness botInVoice(long channelId, String name) {
            var voiceState = mock(net.dv8tion.jda.api.entities.GuildVoiceState.class);
            var channel = voiceChannel(channelId, name);
            when(selfMember.getVoiceState()).thenReturn(voiceState);
            when(voiceState.getChannel()).thenReturn(channel);
            return this;
        }

        public Harness bothInVoice(long channelId, String name) {
            return memberInVoice(channelId, name).botInVoice(channelId, name);
        }

        public Harness withStringOption(String name, String value) {
            OptionMapping option = mock(OptionMapping.class);
            when(event.getOption(name)).thenReturn(option);
            when(option.getAsString()).thenReturn(value);
            return this;
        }

        public Harness withIntOption(String name, int value) {
            OptionMapping option = mock(OptionMapping.class);
            when(event.getOption(name)).thenReturn(option);
            when(option.getAsInt()).thenReturn(value);
            return this;
        }

        public Harness withActivePlayer() {
            LavalinkPlayer player = mock(LavalinkPlayer.class);
            Track track = mock(Track.class);
            when(musicManager.getPlayer()).thenReturn(player);
            when(player.getTrack()).thenReturn(track);
            when(scheduler.getCurrent()).thenReturn(null);
            return this;
        }

        public Harness withNoActivePlayer() {
            when(musicManager.getPlayer()).thenReturn(null);
            when(scheduler.getCurrent()).thenReturn(null);
            return this;
        }

        public Harness withPausedPlayer() {
            LavalinkPlayer player = mock(LavalinkPlayer.class);
            Track track = mock(Track.class);
            when(musicManager.getPlayer()).thenReturn(player);
            when(player.getTrack()).thenReturn(track);
            when(player.getPaused()).thenReturn(true);
            return this;
        }

        public Harness withPlayingPlayer() {
            LavalinkPlayer player = mock(LavalinkPlayer.class);
            Track track = mock(Track.class);
            when(musicManager.getPlayer()).thenReturn(player);
            when(player.getTrack()).thenReturn(track);
            when(player.getPaused()).thenReturn(false);
            return this;
        }

        public Track mockTrack(String title) {
            Track track = mock(Track.class);
            TrackInfo info = mock(TrackInfo.class);
            when(track.getInfo()).thenReturn(info);
            when(info.getTitle()).thenReturn(title);
            when(info.getUri()).thenReturn("https://example.com/" + title);
            when(track.getEncoded()).thenReturn("encoded-" + title);
            return track;
        }

        private static AudioChannelUnion voiceChannel(long channelId, String name) {
            AudioChannelUnion channel = mock(AudioChannelUnion.class);
            when(channel.getIdLong()).thenReturn(channelId);
            when(channel.getName()).thenReturn(name);
            return channel;
        }
    }

    public static Harness harness() {
        return new Harness();
    }

    @SuppressWarnings("unchecked")
    public static InteractionHook mockHook() {
        InteractionHook hook = mock(InteractionHook.class);
        var hookAction = mock(net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction.class);
        when(hook.sendMessageEmbeds(any(MessageEmbed.class), any(MessageEmbed[].class))).thenReturn(hookAction);
        when(hookAction.setEphemeral(true)).thenReturn(hookAction);
        doNothing().when(hookAction).queue();
        return hook;
    }
}
