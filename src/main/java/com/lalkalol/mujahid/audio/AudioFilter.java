package com.lalkalol.mujahid.audio;

import dev.arbjerg.lavalink.client.player.FilterBuilder;
import dev.arbjerg.lavalink.protocol.v4.Band;
import dev.arbjerg.lavalink.protocol.v4.Filters;
import dev.arbjerg.lavalink.protocol.v4.Karaoke;
import dev.arbjerg.lavalink.protocol.v4.Rotation;
import dev.arbjerg.lavalink.protocol.v4.Timescale;

import java.util.List;

public enum AudioFilter {
    OFF("Off") {
        @Override
        public Filters build() {
            return new FilterBuilder().build();
        }
    },
    BASSBOOST("Bass Boost") {
        @Override
        public Filters build() {
            return new FilterBuilder()
                    .setEqualizer(List.of(
                            new Band(0, 0.25f),
                            new Band(1, 0.25f),
                            new Band(2, 0.20f),
                            new Band(3, 0.10f)
                    ))
                    .build();
        }
    },
    NIGHTCORE("Nightcore") {
        @Override
        public Filters build() {
            return new FilterBuilder()
                    .setTimescale(new Timescale(1.25, 1.25, 1.0))
                    .build();
        }
    },
    EIGHT_D("8D") {
        @Override
        public Filters build() {
            return new FilterBuilder()
                    .setRotation(new Rotation(0.2))
                    .build();
        }
    },
    KARAOKE("Karaoke") {
        @Override
        public Filters build() {
            return new FilterBuilder()
                    .setKaraoke(new Karaoke())
                    .build();
        }
    };

    private final String displayName;

    AudioFilter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract Filters build();

    public static AudioFilter from(String value) {
        return switch (value.toLowerCase()) {
            case "off", "none", "clear" -> OFF;
            case "bassboost", "bass" -> BASSBOOST;
            case "nightcore" -> NIGHTCORE;
            case "8d", "eight_d", "rotation" -> EIGHT_D;
            case "karaoke" -> KARAOKE;
            default -> null;
        };
    }
}
