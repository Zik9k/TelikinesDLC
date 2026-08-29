package com.zik9k.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public final class ClientConfig {
    private static final Path FILE = Path.of("config", "telikinesdlc.properties");

    private static boolean animations = true;
    private static boolean hoverEffects = true;
    private static int guiScale = 100;
    private static int overlayOpacity = 54;
    private static int accent = 0;
    private static int avatarIndex = 0;
    private static final Set<String> enabledModules = new LinkedHashSet<>();

    private static boolean triggerClickMode = true;
    private static boolean triggerCritMode;
    private static boolean triggerMobs = true;
    private static boolean triggerAnimals;
    private static boolean triggerPlayers = true;
    private static int triggerCps = 8;

    private ClientConfig() { }

    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(FILE)) { properties.load(input); }

            animations = Boolean.parseBoolean(properties.getProperty("animations", "true"));
            hoverEffects = Boolean.parseBoolean(properties.getProperty("hoverEffects", "true"));
            guiScale = clampInt(parseInt(properties.getProperty("guiScale"), 100), 80, 125);
            overlayOpacity = clampInt(parseInt(properties.getProperty("overlayOpacity"), 54), 20, 85);
            accent = clampInt(parseInt(properties.getProperty("accent"), 0), 0, 2);
            avatarIndex = clampInt(parseInt(properties.getProperty("avatarIndex"), 0), 0, 3);

            enabledModules.clear();
            String storedModules = properties.getProperty("enabledModules", "");
            if (!storedModules.isBlank()) {
                Arrays.stream(storedModules.split(",")).map(String::trim).filter(name -> !name.isEmpty()).forEach(enabledModules::add);
            }

            triggerClickMode = Boolean.parseBoolean(properties.getProperty("triggerClickMode", "true"));
            triggerCritMode = Boolean.parseBoolean(properties.getProperty("triggerCritMode", "false"));
            triggerMobs = Boolean.parseBoolean(properties.getProperty("triggerMobs", "true"));
            triggerAnimals = Boolean.parseBoolean(properties.getProperty("triggerAnimals", "false"));
            triggerPlayers = Boolean.parseBoolean(properties.getProperty("triggerPlayers", "true"));
            triggerCps = clampInt(parseInt(properties.getProperty("triggerCps"), 8), 1, 20);
        } catch (IOException ignored) { reset(); }
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
            properties.setProperty("avatarIndex", Integer.toString(avatarIndex));
            properties.setProperty("enabledModules", String.join(",", enabledModules));
            properties.setProperty("triggerClickMode", Boolean.toString(triggerClickMode));
            properties.setProperty("triggerCritMode", Boolean.toString(triggerCritMode));
            properties.setProperty("triggerMobs", Boolean.toString(triggerMobs));
            properties.setProperty("triggerAnimals", Boolean.toString(triggerAnimals));
            properties.setProperty("triggerPlayers", Boolean.toString(triggerPlayers));
            properties.setProperty("triggerCps", Integer.toString(triggerCps));
            try (OutputStream output = Files.newOutputStream(FILE)) { properties.store(output, "TelikinesDLC client configuration"); }
        } catch (IOException ignored) { }
    }

    public static void reset() {
        animations = true; hoverEffects = true; guiScale = 100; overlayOpacity = 54; accent = 0; avatarIndex = 0;
        enabledModules.clear();
        triggerClickMode = true; triggerCritMode = false; triggerMobs = true; triggerAnimals = false; triggerPlayers = true; triggerCps = 8;
        save();
    }

    private static int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return fallback; } }
    private static int clampInt(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    public static boolean animations() { return animations; }
    public static void setAnimations(boolean value) { animations = value; save(); }
    public static boolean hoverEffects() { return hoverEffects; }
    public static void setHoverEffects(boolean value) { hoverEffects = value; save(); }
    public static int guiScale() { return guiScale; }
    public static void setGuiScale(int value) { guiScale = clampInt(value, 80, 125); save(); }
    public static int overlayOpacity() { return overlayOpacity; }
    public static void setOverlayOpacity(int value) { overlayOpacity = clampInt(value, 20, 85); save(); }
    public static int accent() { return accent; }
    public static void setAccent(int value) { accent = clampInt(value, 0, 2); save(); }
    public static int avatarIndex() { return avatarIndex; }
    public static void setAvatarIndex(int value) { avatarIndex = clampInt(value, 0, 3); save(); }
    public static boolean isModuleEnabled(String moduleName) { return enabledModules.contains(moduleName); }
    public static void setModuleEnabled(String moduleName, boolean enabled) { if (enabled) enabledModules.add(moduleName); else enabledModules.remove(moduleName); save(); }

    public static boolean triggerClickMode() { return triggerClickMode; }
    public static boolean triggerCritMode() { return triggerCritMode; }
    public static boolean triggerMobs() { return triggerMobs; }
    public static boolean triggerAnimals() { return triggerAnimals; }
    public static boolean triggerPlayers() { return triggerPlayers; }
    public static int triggerCps() { return triggerCps; }
    public static void setTriggerClickMode(boolean value) { triggerClickMode = value; save(); }
    public static void setTriggerCritMode(boolean value) { triggerCritMode = value; save(); }
    public static void setTriggerMobs(boolean value) { triggerMobs = value; save(); }
    public static void setTriggerAnimals(boolean value) { triggerAnimals = value; save(); }
    public static void setTriggerPlayers(boolean value) { triggerPlayers = value; save(); }
    public static void setTriggerCps(int value) { triggerCps = clampInt(value, 1, 20); save(); }
}
