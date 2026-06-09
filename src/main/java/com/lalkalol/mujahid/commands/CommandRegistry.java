package com.lalkalol.mujahid.commands;

import com.lalkalol.mujahid.audio.LavalinkManager;
import com.lalkalol.mujahid.commands.impl.ClearCommand;
import com.lalkalol.mujahid.commands.impl.FilterCommand;
import com.lalkalol.mujahid.commands.impl.JoinCommand;
import com.lalkalol.mujahid.commands.impl.LeaveCommand;
import com.lalkalol.mujahid.commands.impl.LoopCommand;
import com.lalkalol.mujahid.commands.impl.NowPlayingCommand;
import com.lalkalol.mujahid.commands.impl.PauseCommand;
import com.lalkalol.mujahid.commands.impl.PlayCommand;
import com.lalkalol.mujahid.commands.impl.PlayFileCommand;
import com.lalkalol.mujahid.commands.impl.PlayNextCommand;
import com.lalkalol.mujahid.commands.impl.PlaylistCommand;
import com.lalkalol.mujahid.commands.impl.QueueCommand;
import com.lalkalol.mujahid.commands.impl.RemoveCommand;
import com.lalkalol.mujahid.commands.impl.ResumeCommand;
import com.lalkalol.mujahid.commands.impl.SeekCommand;
import com.lalkalol.mujahid.commands.impl.ShuffleCommand;
import com.lalkalol.mujahid.commands.impl.SkipCommand;
import com.lalkalol.mujahid.commands.impl.StopCommand;
import com.lalkalol.mujahid.commands.impl.VolumeCommand;
import com.lalkalol.mujahid.db.PlaylistRepository;
import com.lalkalol.mujahid.util.Embeds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);

    private final LavalinkManager lavalink;
    private final PlaylistRepository playlists;
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandRegistry(LavalinkManager lavalink, PlaylistRepository playlists) {
        this.lavalink = lavalink;
        this.playlists = playlists;
    }

    public void registerDefaults() {
        register(
                new PlayCommand(),
                new PlayNextCommand(),
                new PlayFileCommand(),
                new JoinCommand(),
                new LeaveCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new StopCommand(),
                new VolumeCommand(),
                new QueueCommand(),
                new NowPlayingCommand(),
                new LoopCommand(),
                new ShuffleCommand(),
                new SeekCommand(),
                new RemoveCommand(),
                new ClearCommand(),
                new FilterCommand(),
                new PlaylistCommand()
        );
        log.info("Registered {} slash commands", commands.size());
    }

    public void register(Command... toAdd) {
        for (Command command : toAdd) {
            commands.put(command.name(), command);
        }
    }

    public List<SlashCommandData> commandData() {
        return commands.values().stream().map(Command::data).toList();
    }

    public void handle(SlashCommandInteractionEvent event) {
        Command command = commands.get(event.getName());
        if (command == null) {
            event.replyEmbeds(Embeds.error("Unknown command.")).setEphemeral(true).queue();
            return;
        }

        if (event.getGuild() == null) {
            event.replyEmbeds(Embeds.warning("This command can only be used in a server."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userId = event.getUser() != null ? event.getUser().getId() : "unknown";
        log.info("Executing /{} in guild {} (user {})", event.getName(), event.getGuild().getId(), userId);

        try {
            command.execute(new CommandContext(event, lavalink, playlists));
        } catch (Exception e) {
            log.error("Error while executing /{} in guild {}", event.getName(), event.getGuild().getId(), e);
            var embed = Embeds.error("Something went wrong: " + e.getMessage());
            if (event.isAcknowledged()) {
                event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            } else {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            }
        }
    }
}
