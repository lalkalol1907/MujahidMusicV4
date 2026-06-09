package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.audio.TrackUserData;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.util.Embeds;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.NoMatches;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class PlaySupport {
    private PlaySupport() {
    }

    public static void loadAndPlay(CommandContext ctx, String identifier) {
        if (!ctx.ensureConnected()) {
            return;
        }

        var event = ctx.getEvent();
        event.deferReply().queue();

        var manager = ctx.getMusicManager();
        var userData = new TrackUserData(event.getUser().getIdLong(), event.getChannelIdLong());

        manager.getLink().loadItem(identifier).subscribe(result -> {
            if (result instanceof TrackLoaded trackLoaded) {
                Track track = withData(trackLoaded.getTrack(), userData);
                boolean started = manager.getScheduler().enqueue(track);
                sendPlayed(event, track, started, manager.getScheduler().queueSize());
            } else if (result instanceof SearchResult searchResult) {
                var tracks = searchResult.getTracks();
                if (tracks.isEmpty()) {
                    event.getHook().sendMessageEmbeds(Embeds.warning("No matches found.")).queue();
                } else {
                    Track track = withData(tracks.getFirst(), userData);
                    boolean started = manager.getScheduler().enqueue(track);
                    sendPlayed(event, track, started, manager.getScheduler().queueSize());
                }
            } else if (result instanceof PlaylistLoaded playlistLoaded) {
                var tracks = playlistLoaded.getTracks().stream()
                        .map(track -> withData(track, userData))
                        .toList();
                manager.getScheduler().enqueueAll(tracks);
                event.getHook().sendMessageEmbeds(
                        Embeds.success("Added **" + tracks.size() + "** tracks from playlist **"
                                + playlistLoaded.getInfo().getName() + "**.")
                ).queue();
            } else if (result instanceof NoMatches) {
                event.getHook().sendMessageEmbeds(Embeds.warning("No matches found for your input.")).queue();
            } else if (result instanceof LoadFailed loadFailed) {
                event.getHook().sendMessageEmbeds(
                        Embeds.error("Failed to load: " + loadFailed.getException().getMessage())
                ).queue();
            }
        });
    }

    private static Track withData(Track track, TrackUserData data) {
        track.setUserData(data);
        return track;
    }

    private static void sendPlayed(
            SlashCommandInteractionEvent event,
            Track track,
            boolean startedNow,
            int queueSize
    ) {
        var info = track.getInfo();
        var builder = Embeds.music()
                .setAuthor(startedNow ? "Now playing" : "Added to queue")
                .setTitle(info.getTitle().isBlank() ? "Unknown" : info.getTitle(), info.getUri())
                .setDescription("by " + info.getAuthor());
        if (!startedNow) {
            builder.setFooter("Position in queue: " + queueSize);
        }
        event.getHook().sendMessageEmbeds(builder.build()).queue();
    }
}
