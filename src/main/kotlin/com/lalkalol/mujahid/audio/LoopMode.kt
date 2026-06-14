package com.lalkalol.mujahid.audio

enum class LoopMode {
    OFF,
    TRACK,
    QUEUE,
    ;

    companion object {
        fun from(value: String): LoopMode? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
