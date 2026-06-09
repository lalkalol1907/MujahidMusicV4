package com.lalkalol.mujahid.commands.impl;

import com.lalkalol.mujahid.audio.TrackUserData;
import com.lalkalol.mujahid.commands.Command;
import com.lalkalol.mujahid.commands.CommandContext;
import com.lalkalol.mujahid.db.PlaylistSummary;
import com.lalkalol.mujahid.db.StoredTrack;
import com.lalkalol.mujahid.util.Embeds;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public class PlaylistCommand implements Command {
    @Override
    public String name() {
        return "playlist";
    }

    @Override
    public net.dv8tion.jda.api.interactions.commands.build.SlashCommandData data() {
        return Commands.slash("playlist", "Manage your personal saved playlists.")
                .addSubcommands(
                        new SubcommandData("save", "Save the current track and queue as a playlist")
                                .addOption(OptionType.STRING, "name", "Playlist name", true),
                        new SubcommandData("load", "Load a saved playlist into the queue")
                                .addOption(OptionType.STRING, "name", "Playlist name", true),
                        new SubcommandData("list", "List your saved playlists"),
                        new SubcommandData("delete", "Delete a saved playlist")
                                .addOption(OptionType.STRING, "name", "Playlist name", true)
                );
    }

    @Override
    public void execute(CommandContext ctx) {
        String subcommand = ctx.getEvent().getSubcommandName();
        switch (subcommand) {
            case "save" -> save(ctx);
            case "load" -> load(ctx);
            case "list" -> list(ctx);
            case "delete" -> delete(ctx);
            default -> ctx.replyEphemeral("Unknown subcommand.");
        }
    }

    private void save(CommandContext ctx) {
        String name = ctx.getEvent().getOption("name").getAsString().trim();
        if (name.isEmpty()) {
            ctx.replyEphemeral("Playlist name cannot be empty.");
            return;
        }

        var scheduler = ctx.getMusicManager().getScheduler();
        List<Track> tracks = new ArrayList<>();
        Track current = scheduler.getCurrent();
        if (current != null) {
            tracks.add(current);
        }
        tracks.addAll(scheduler.queueSnapshot());

        if (tracks.isEmpty()) {
            ctx.replyEphemeral("There is nothing playing or queued to save.");
            return;
        }

        List<StoredTrack> stored = tracks.stream()
                .map(track -> new StoredTrack(track.getEncoded(), track.getInfo().getTitle(), track.getInfo().getUri()))
                .toList();
        int count = ctx.getPlaylists().save(ctx.getEvent().getUser().getIdLong(), name, stored);
        ctx.reply("Saved **" + count + "** track(s) to playlist **" + name + "**.");
    }

    private void load(CommandContext ctx) {
        String name = ctx.getEvent().getOption("name").getAsString().trim();
        List<StoredTrack> stored = ctx.getPlaylists().load(ctx.getEvent().getUser().getIdLong(), name);
        if (stored == null) {
            ctx.replyEphemeral("You don't have a playlist named **" + name + "**.");
            return;
        }
        if (stored.isEmpty()) {
            ctx.replyEphemeral("Playlist **" + name + "** is empty.");
            return;
        }
        if (!ctx.ensureConnected()) {
            return;
        }

        ctx.getEvent().deferReply().queue();
        var userData = new TrackUserData(ctx.getEvent().getUser().getIdLong(), ctx.getEvent().getChannelIdLong());
        var node = ctx.getMusicManager().getLink().getNode();

        node.decodeTracks(stored.stream().map(StoredTrack::encoded).toList()).subscribe(
                tracks -> {
                    tracks.forEach(track -> track.setUserData(userData));
                    ctx.getMusicManager().getScheduler().enqueueAll(tracks);
                    ctx.getEvent().getHook().sendMessageEmbeds(
                            Embeds.success("Loaded **" + tracks.size() + "** track(s) from **" + name + "**.")
                    ).queue();
                },
                error -> ctx.getEvent().getHook().sendMessageEmbeds(
                        Embeds.error("Failed to load playlist: " + error.getMessage())
                ).queue()
        );
    }

    private void list(CommandContext ctx) {
        List<PlaylistSummary> playlists = ctx.getPlaylists().list(ctx.getEvent().getUser().getIdLong());
        if (playlists.isEmpty()) {
            ctx.replyEphemeral("You have no saved playlists. Use `/playlist save` to create one.");
            return;
        }
        StringBuilder description = new StringBuilder();
        for (PlaylistSummary playlist : playlists) {
            description.append("🎵 **")
                    .append(playlist.name())
                    .append("** — ")
                    .append(playlist.trackCount())
                    .append(" track(s)\n");
        }
        var embed = Embeds.music()
                .setTitle("Your playlists")
                .setDescription(description.toString().trim())
                .build();
        ctx.getEvent().replyEmbeds(embed).setEphemeral(true).queue();
    }

    private void delete(CommandContext ctx) {
        String name = ctx.getEvent().getOption("name").getAsString().trim();
        if (ctx.getPlaylists().delete(ctx.getEvent().getUser().getIdLong(), name)) {
            ctx.reply("Deleted playlist **" + name + "**.");
            return;
        }
        ctx.replyEphemeral("You don't have a playlist named **" + name + "**.");
    }
}
