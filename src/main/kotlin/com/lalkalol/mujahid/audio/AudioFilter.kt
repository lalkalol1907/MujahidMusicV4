package com.lalkalol.mujahid.audio

import dev.arbjerg.lavalink.client.player.FilterBuilder
import dev.arbjerg.lavalink.protocol.v4.Band
import dev.arbjerg.lavalink.protocol.v4.Filters
import dev.arbjerg.lavalink.protocol.v4.Rotation
import dev.arbjerg.lavalink.protocol.v4.Timescale

enum class AudioFilter(val displayName: String) {
    OFF("Off") {
        override fun build(): Filters = FilterBuilder().build()
    },
    BASSBOOST("Bass Boost") {
        override fun build(): Filters = FilterBuilder()
            .setEqualizer(
                listOf(
                    Band(0, 0.25f),
                    Band(1, 0.25f),
                    Band(2, 0.20f),
                    Band(3, 0.10f),
                ),
            )
            .build()
    },
    NIGHTCORE("Nightcore") {
        override fun build(): Filters = FilterBuilder()
            .setTimescale(Timescale(speed = 1.25, pitch = 1.25, rate = 1.0))
            .build()
    },
    EIGHT_D("8D") {
        override fun build(): Filters = FilterBuilder()
            .setRotation(Rotation(rotationHz = 0.2))
            .build()
    },
    KARAOKE("Karaoke") {
        override fun build(): Filters = FilterBuilder()
            .setKaraoke(dev.arbjerg.lavalink.protocol.v4.Karaoke())
            .build()
    };

    abstract fun build(): Filters

    companion object {
        fun from(value: String): AudioFilter? = when (value.lowercase()) {
            "off", "none", "clear" -> OFF
            "bassboost", "bass" -> BASSBOOST
            "nightcore" -> NIGHTCORE
            "8d", "eight_d", "rotation" -> EIGHT_D
            "karaoke" -> KARAOKE
            else -> null
        }
    }
}
