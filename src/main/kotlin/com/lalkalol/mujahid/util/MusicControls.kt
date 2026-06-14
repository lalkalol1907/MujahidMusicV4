package com.lalkalol.mujahid.util

import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button

object MusicControls {
    const val PREFIX: String = "muj:"

    fun skipId(guildId: Long): String = "${PREFIX}skip:$guildId"

    fun pauseId(guildId: Long): String = "${PREFIX}pause:$guildId"

    fun resumeId(guildId: Long): String = "${PREFIX}resume:$guildId"

    fun stopId(guildId: Long): String = "${PREFIX}stop:$guildId"

    fun queuePrevId(guildId: Long, page: Int): String = "${PREFIX}qprev:$guildId:$page"

    fun queueNextId(guildId: Long, page: Int): String = "${PREFIX}qnext:$guildId:$page"

    fun playbackRow(guildId: Long, paused: Boolean): List<ActionRow> {
        val buttons = mutableListOf<Button>()
        buttons.add(Button.danger(skipId(guildId), "⏭ Skip"))
        if (paused) {
            buttons.add(Button.success(resumeId(guildId), "▶ Resume"))
        } else {
            buttons.add(Button.primary(pauseId(guildId), "⏸ Pause"))
        }
        buttons.add(Button.secondary(stopId(guildId), "⏹ Stop"))
        return listOf(ActionRow.of(buttons))
    }

    fun queueNavRow(guildId: Long, page: Int, totalPages: Int): List<ActionRow> {
        val buttons = listOf(
            Button.secondary(queuePrevId(guildId, page), "◀ Prev").withDisabled(page <= 1),
            Button.secondary("muj:qpage:$guildId:$page", "$page/$totalPages").withDisabled(true),
            Button.secondary(queueNextId(guildId, page), "Next ▶").withDisabled(page >= totalPages),
        )
        return listOf(ActionRow.of(buttons))
    }
}
