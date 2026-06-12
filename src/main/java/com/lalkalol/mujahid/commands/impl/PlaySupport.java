package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.audio.AddResult;
import com.lalkalol.mujahid.audio.TrackScheduler;
import com.lalkalol.mujahid.audio.TrackUserData;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.metrics.MetricsHolder;
import com.lalkalol.mujahid.util.Embeds;
import com.lalkalol.mujahid.util.MusicControls;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.NoMatches;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlaySupport {
    private static final Logger log = LoggerFactory.getLogger(PlaySupport.class);

    private PlaySupport() {
    }

    public static void loadAndPlay(CommandContext ctx, String identifier) {
        loadTrack(ctx, identifier, false);
    }

    public static void loadAndEnqueueNext(CommandContext ctx, String identifier) {
        loadTrack(ctx, identifier, true);
    }

    private static void loadTrack(CommandContext ctx, String identifier, boolean playNext) {
        if (!ctx.ensureConnected()) {
            return;
        }

        var event = ctx.getEvent();
        event.deferReply().queue();

        var manager = ctx.getMusicManager();
        var userData = new TrackUserData(event.getUser().getIdLong(), event.getChannelIdLong());
        long guildId = ctx.getGuild().getIdLong();

        log.info("Loading '{}' for guild {} (playNext={})", identifier, guildId, playNext);

        manager.getLink().loadItem(identifier).subscribe(
                result -> handleLoadResult(event, manager.getScheduler(), userData, guildId, playNext, result),
                error -> {
                    log.error("Load failed for '{}' in guild {}", identifier, guildId, error);
                    event.getHook().sendMessageEmbeds(
                            Embeds.error("Failed to load: " + error.getMessage())
                    ).queue();
                }
        );
    }

    private static void handleLoadResult(
            SlashCommandInteractionEvent event,
            TrackScheduler scheduler,
            TrackUserData userData,
            long guildId,
            boolean playNext,
            dev.arbjerg.lavalink.client.player.LavalinkLoadResult result
    ) {
        if (result instanceof TrackLoaded trackLoaded) {
            Track track = withData(trackLoaded.getTrack(), userData);
            AddResult addResult = playNext ? scheduler.enqueueNext(track) : scheduler.enqueue(track);
            MetricsHolder.get().recordTrackPlayed("url");
            sendAdded(event, track, addResult, scheduler.queueSize(), guildId, playNext);
        } else if (result instanceof SearchResult searchResult) {
            var tracks = searchResult.getTracks();
            if (tracks.isEmpty()) {
                event.getHook().sendMessageEmbeds(Embeds.warning("No matches found.")).queue();
            } else {
                Track track = withData(tracks.getFirst(), userData);
                AddResult addResult = playNext ? scheduler.enqueueNext(track) : scheduler.enqueue(track);
                MetricsHolder.get().recordTrackPlayed("search");
                sendAdded(event, track, addResult, scheduler.queueSize(), guildId, playNext);
            }
        } else if (result instanceof PlaylistLoaded playlistLoaded) {
            var tracks = playlistLoaded.getTracks().stream()
                    .map(track -> withData(track, userData))
                    .toList();
            scheduler.enqueueAll(tracks);
            MetricsHolder.get().recordTrackPlayed("playlist");
            event.getHook().sendMessageEmbeds(
                    Embeds.success("Added **" + tracks.size() + "** tracks from playlist **"
                            + playlistLoaded.getInfo().getName() + "**.")
            ).queue();
        } else if (result instanceof NoMatches) {
            MetricsHolder.get().recordTrackLoadFailure("no_matches");
            event.getHook().sendMessageEmbeds(Embeds.warning("No matches found for your input.")).queue();
        } else if (result instanceof LoadFailed loadFailed) {
            log.warn("Lavalink load failed in guild {}: {}", guildId, loadFailed.getException().getMessage());
            MetricsHolder.get().recordTrackLoadFailure("lavalink_error");
            event.getHook().sendMessageEmbeds(
                    Embeds.error("Failed to load: " + loadFailed.getException().getMessage())
            ).queue();
        }
    }

    private static Track withData(Track track, TrackUserData data) {
        track.setUserData(data);
        return track;
    }

    private static void sendAdded(
            SlashCommandInteractionEvent event,
            Track track,
            AddResult addResult,
            int queueSize,
            long guildId,
            boolean playNext
    ) {
        if (addResult == AddResult.REJECTED_FULL) {
            event.getHook().sendMessageEmbeds(
                    Embeds.warning("Queue is full (" + TrackScheduler.MAX_QUEUE_SIZE + " tracks).")
            ).queue();
            return;
        }

        var info = track.getInfo();
        String author = switch (addResult) {
            case STARTED_NOW -> "Now playing";
            case QUEUED -> playNext ? "Playing next" : "Added to queue";
            case REJECTED_FULL -> throw new IllegalStateException();
        };

        var builder = Embeds.music()
                .setAuthor(author)
                .setTitle(info.getTitle().isBlank() ? "Unknown" : info.getTitle(), info.getUri())
                .setDescription("by " + info.getAuthor());

        if (addResult == AddResult.QUEUED) {
            int position = playNext ? 1 : queueSize;
            builder.setFooter("Position in queue: " + position);
        }

        var hook = event.getHook().sendMessageEmbeds(builder.build());
        if (addResult == AddResult.STARTED_NOW) {
            hook.setComponents(MusicControls.playbackRow(guildId, false));
        }
        hook.queue();
    }
}
