package com.lalkalol.mujahid.util

import com.lalkalol.mujahid.audio.TrackScheduler
import dev.arbjerg.lavalink.client.player.Track
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.entities.MessageEmbed

object QueuePages {
    const val PAGE_SIZE: Int = 10

    data class PageView(
        val embed: MessageEmbed,
        val components: List<ActionRow>,
        val page: Int,
        val totalPages: Int,
    )

    fun totalPages(queueSize: Int): Int {
        if (queueSize <= 0) {
            return 1
        }
        return (queueSize + PAGE_SIZE - 1) / PAGE_SIZE
    }

    fun clampPage(page: Int, totalPages: Int): Int = maxOf(1, minOf(page, totalPages))

    fun build(guildId: Long, scheduler: TrackScheduler, requestedPage: Int): PageView {
        val current = scheduler.getCurrent()
        val queue = scheduler.queueSnapshot()
        val pages = totalPages(queue.size)
        val page = clampPage(requestedPage, pages)

        val description = buildString {
            if (current != null) {
                append("**Now playing:** [")
                append(current.info.title)
                append("](")
                append(current.info.uri)
                append(") `")
                append(Format.duration(current.info.length, current.info.isStream))
                append("`\n\n")
            }

            if (queue.isEmpty()) {
                append("*No tracks queued.*")
            } else {
                append("**Up next** (page $page/$pages):\n")
                val start = (page - 1) * PAGE_SIZE
                val end = minOf(start + PAGE_SIZE, queue.size)
                for (i in start until end) {
                    val track = queue[i]
                    append("`")
                    append(i + 1)
                    append(".` ")
                    append(track.info.title)
                    append(" `")
                    append(Format.duration(track.info.length, track.info.isStream))
                    append("`\n")
                }
            }
        }

        val embed = Embeds.music()
            .setTitle("🎶 Queue")
            .setDescription(description)
            .setFooter(
                "Loop: ${scheduler.getLoopMode()}" +
                    " • Filter: ${scheduler.getFilter().displayName}" +
                    " • Volume: ${scheduler.getVolume()}% • " +
                    "${queue.size} in queue",
            )
            .build()

        val components = if (queue.isEmpty()) {
            emptyList()
        } else {
            MusicControls.queueNavRow(guildId, page, pages)
        }

        return PageView(embed, components, page, pages)
    }
}
