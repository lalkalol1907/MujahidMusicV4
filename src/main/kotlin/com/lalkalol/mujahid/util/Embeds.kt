package com.lalkalol.mujahid.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

object Embeds {
    val BRAND: Color = Color(0x5865F2)
    val SUCCESS: Color = Color(0x57F287)
    val WARNING: Color = Color(0xFEE75C)
    val ERROR: Color = Color(0xED4245)
    val MUSIC: Color = Color(0xEB459E)

    fun success(text: String): MessageEmbed = simple(SUCCESS, text)

    fun warning(text: String): MessageEmbed = simple(WARNING, text)

    fun error(text: String): MessageEmbed = simple(ERROR, text)

    fun info(text: String): MessageEmbed = simple(BRAND, text)

    fun music(): EmbedBuilder = EmbedBuilder().setColor(MUSIC)

    private fun simple(color: Color, description: String): MessageEmbed =
        EmbedBuilder()
            .setColor(color)
            .setDescription(description)
            .build()
}
