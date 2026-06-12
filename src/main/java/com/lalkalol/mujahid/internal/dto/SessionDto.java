package com.lalkalol.mujahid.internal.dto;

public record SessionDto(
        String guildId,
        String guildName,
        String nodeName,
        String trackTitle,
        String trackAuthor,
        long positionMs,
        long lengthMs,
        boolean paused,
        int volume,
        int queueSize,
        String loopMode
) {
}
