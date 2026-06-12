package com.lalkalol.mujahid.internal.dto;

public record NodeDto(
        String name,
        String uri,
        boolean connected,
        String sessionId,
        int players,
        int playing
) {
}
