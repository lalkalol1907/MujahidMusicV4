package com.lalkalol.mujahid.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

public final class Embeds {
    public static final Color BRAND = new Color(0x5865F2);
    public static final Color SUCCESS = new Color(0x57F287);
    public static final Color WARNING = new Color(0xFEE75C);
    public static final Color ERROR = new Color(0xED4245);
    public static final Color MUSIC = new Color(0xEB459E);

    private Embeds() {
    }

    public static MessageEmbed success(String text) {
        return simple(SUCCESS, text);
    }

    public static MessageEmbed warning(String text) {
        return simple(WARNING, text);
    }

    public static MessageEmbed error(String text) {
        return simple(ERROR, text);
    }

    public static MessageEmbed info(String text) {
        return simple(BRAND, text);
    }

    public static EmbedBuilder music() {
        return new EmbedBuilder().setColor(MUSIC);
    }

    private static MessageEmbed simple(Color color, String description) {
        return new EmbedBuilder()
                .setColor(color)
                .setDescription(description)
                .build();
    }
}
