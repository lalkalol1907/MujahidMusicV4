package com.lalkalol.mujahid.commands;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.commands.impl.LoopCommand;
import com.lalkalol.mujahid.db.PlaylistRepository;
import com.lalkalol.mujahid.metrics.BotMetrics;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandRegistryTest {

    @Mock
    private LavalinkManager lavalink;

    @Mock
    private PlaylistRepository playlists;

    @Mock
    private BotMetrics metrics;

    @Mock
    private SlashCommandInteractionEvent event;

    private CommandRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry(lavalink, playlists, metrics);
        registry.register(new LoopCommand());
    }

    @Test
    void handle_unknownCommandRepliesWithError() {
        when(event.getName()).thenReturn("nope");
        ReplyCallbackAction reply = mock(ReplyCallbackAction.class);
        when(event.replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class)))
                .thenReturn(reply);
        when(reply.setEphemeral(true)).thenReturn(reply);

        registry.handle(event);

        verify(event).replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class));
        verify(reply).setEphemeral(true);
    }

    @Test
    void handle_dmUsageIsRejected() {
        when(event.getName()).thenReturn("loop");
        when(event.getGuild()).thenReturn(null);
        ReplyCallbackAction reply = mock(ReplyCallbackAction.class);
        when(event.replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class)))
                .thenReturn(reply);
        when(reply.setEphemeral(true)).thenReturn(reply);

        registry.handle(event);

        verify(event).replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class));
    }

    @Test
    void handle_commandExceptionSendsErrorEmbed() {
        when(event.getName()).thenReturn("loop");
        when(event.getGuild()).thenReturn(mock(net.dv8tion.jda.api.entities.Guild.class));

        ReplyCallbackAction reply = mock(ReplyCallbackAction.class);
        when(event.replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class)))
                .thenReturn(reply);
        when(reply.setEphemeral(true)).thenReturn(reply);

        registry.handle(event);

        verify(event).replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class));
    }

    @Test
    void handle_acknowledgedExceptionUsesHook() {
        when(event.getName()).thenReturn("loop");
        when(event.getGuild()).thenReturn(mock(net.dv8tion.jda.api.entities.Guild.class));
        when(event.isAcknowledged()).thenReturn(true);

        InteractionHook hook = CommandTestFixtures.mockHook();
        when(event.getHook()).thenReturn(hook);

        registry.handle(event);

        verify(hook).sendMessageEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class));
        verify(event, never()).replyEmbeds(any(net.dv8tion.jda.api.entities.MessageEmbed.class), any(net.dv8tion.jda.api.entities.MessageEmbed[].class));
    }
}
