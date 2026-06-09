package com.lalkalol.mujahid.audio;

public enum LoopMode {
    OFF,
    TRACK,
    QUEUE;

    public static LoopMode from(String value) {
        for (LoopMode mode : values()) {
            if (mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return null;
    }
}
