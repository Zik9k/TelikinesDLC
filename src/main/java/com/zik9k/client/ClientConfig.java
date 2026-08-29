package com.zik9k.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientConfig {
    private static final Path FILE = Path.of("config", "telikinesdlc.properties");

    private static boolean animations = true;
    private static boolean hoverEffects = true;
    private static int guiScale = 100;
    private static int overlayOpacity = 54;
    private static int accent = 0;

    private ClientConfig() {
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                save();
                return;
            }

            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(FILE)) {
                properties.load(input);
            }

            animations = Boolean.parseBoolean(properties.getProperty("animations", "true"));
            hoverEffects = Boolean.parseBoolean(properties.getProperty("hoverEffects", "true"));
            guiScale = clampInt(parseInt(properties.getProperty("guiScale"), 100), 80, 125);
            overlayOpacity = clampInt(parseInt(properties.getProperty("overlayOpacity"), 54), 20, 85);
            accent = clampInt(parseInt(properties.getProperty("accent"), 0), 0, 2);
        } catch (IOException ignored) {
            reset();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Properties properties = new Properties();
            properties.setProperty("animations", Boolean.toString(animations));
            properties.setProperty("hoverEffects", Boolean.toString(hoverEffects));
            properties.setProperty("guiScale", Integer.toString(guiScale));
            properties.setProperty("overlayOpacity", Integer.toString(overlayOpacity));
            properties.setProperty("accent", Integer.toString(accent));
            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "TelikinesDLC client configuration");
            }
        } catch (IOException ignored) {
        }
    }

    public static void reset() {
        animations = true;
        hoverEffects = true;
        guiScale = 100;
        overlayOpacity = 54;
        accent = 0;
        save();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean animations() {
        return animations;
    }

    public static void setAnimations(boolean value) {
        animations = value;
        save();
    }

    public static boolean hoverEffects() {
        return hoverEffects;
    }

    public static void setHoverEffects(boolean value) {
        hoverEffects = value;
        save();
    }

    public static int guiScale() {
        return guiScale;
    }

    public static void setGuiScale(int value) {
        guiScale = clampInt(value, 80, 125);
        save();
    }

    public static int overlayOpacity() {
        return overlayOpacity;
    }

    public static void setOverlayOpacity(int value) {
        overlayOpacity = clampInt(value, 20, 85);
        save();
    }

    public static int accent() {
        return accent;
    }

    public static void setAccent(int value) {
        accent = clampInt(value, 0, 2);
        save();
    }
}
