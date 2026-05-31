package com.lalkalol.mujahid.audio

import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkPlayer

class GuildMusicManager(
    val guildId: Long,
    val manager: LavalinkManager,
) {
    val scheduler = TrackScheduler(this)

    val link: Link
        get() = manager.client.getOrCreateLink(guildId)

    val cachedLink: Link?
        get() = manager.client.getLinkIfCached(guildId)

    val player: LavalinkPlayer?
        get() = cachedLink?.cachedPlayer
}
