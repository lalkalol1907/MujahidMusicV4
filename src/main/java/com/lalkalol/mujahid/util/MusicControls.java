package com.lalkalol.mujahid.util;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public final class MusicControls {
    public static final String PREFIX = "muj:";

    private MusicControls() {
    }

    public static String skipId(long guildId) {
        return PREFIX + "skip:" + guildId;
    }

    public static String pauseId(long guildId) {
        return PREFIX + "pause:" + guildId;
    }

    public static String resumeId(long guildId) {
        return PREFIX + "resume:" + guildId;
    }

    public static String stopId(long guildId) {
        return PREFIX + "stop:" + guildId;
    }

    public static String queuePrevId(long guildId, int page) {
        return PREFIX + "qprev:" + guildId + ":" + page;
    }

    public static String queueNextId(long guildId, int page) {
        return PREFIX + "qnext:" + guildId + ":" + page;
    }

    public static List<ActionRow> playbackRow(long guildId, boolean paused) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.danger(skipId(guildId), "⏭ Skip"));
        if (paused) {
            buttons.add(Button.success(resumeId(guildId), "▶ Resume"));
        } else {
            buttons.add(Button.primary(pauseId(guildId), "⏸ Pause"));
        }
        buttons.add(Button.secondary(stopId(guildId), "⏹ Stop"));
        return List.of(ActionRow.of(buttons));
    }

    public static List<ActionRow> queueNavRow(long guildId, int page, int totalPages) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.secondary(queuePrevId(guildId, page), "◀ Prev").withDisabled(page <= 1));
        buttons.add(Button.secondary("muj:qpage:" + guildId + ":" + page, page + "/" + totalPages).withDisabled(true));
        buttons.add(Button.secondary(queueNextId(guildId, page), "Next ▶").withDisabled(page >= totalPages));
        return List.of(ActionRow.of(buttons));
    }
}
