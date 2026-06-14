package com.lalkalol.mujahid.audio

import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkPlayer

class GuildMusicManager(
    val guildId: Long,
    private val manager: LavalinkManager,
) {
    val scheduler: TrackScheduler = TrackScheduler(this)

    fun getManager(): LavalinkManager = manager

    fun getLink(): Link = manager.client.getOrCreateLink(guildId)

    fun getCachedLink(): Link? = manager.client.getLinkIfCached(guildId)

    fun getPlayer(): LavalinkPlayer? = getCachedLink()?.cachedPlayer
}
