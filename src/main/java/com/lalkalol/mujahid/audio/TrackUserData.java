package com.lalkalol.mujahid.audio;

public record TrackUserData(long requesterId, long textChannelId) {
    public TrackUserData() {
        this(0L, 0L);
    }
}
