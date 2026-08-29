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
    private static boolean triggerClickMode = true, triggerCritMode, triggerMobs = true, triggerAnimals, triggerPlayers = true;
    private static int triggerCps = 8;
    private static boolean espPlayers = true, espMobs = true, espAnimals;
    private static int espRange = 64;
    private static int blockOverlayAlpha = 70;
    private static int blockOverlayOutlineAlpha = 190;

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
            if (!storedModules.isBlank()) Arrays.stream(storedModules.split(",")).map(String::trim).filter(n -> !n.isEmpty()).forEach(enabledModules::add);
            triggerClickMode = Boolean.parseBoolean(properties.getProperty("triggerClickMode", "true"));
            triggerCritMode = Boolean.parseBoolean(properties.getProperty("triggerCritMode", "false"));
            triggerMobs = Boolean.parseBoolean(properties.getProperty("triggerMobs", "true"));
            triggerAnimals = Boolean.parseBoolean(properties.getProperty("triggerAnimals", "false"));
            triggerPlayers = Boolean.parseBoolean(properties.getProperty("triggerPlayers", "true"));
            triggerCps = clampInt(parseInt(properties.getProperty("triggerCps"), 8), 1, 20);
            espPlayers = Boolean.parseBoolean(properties.getProperty("espPlayers", "true"));
            espMobs = Boolean.parseBoolean(properties.getProperty("espMobs", "true"));
            espAnimals = Boolean.parseBoolean(properties.getProperty("espAnimals", "false"));
            espRange = clampInt(parseInt(properties.getProperty("espRange"), 64), 8, 128);
            blockOverlayAlpha = clampInt(parseInt(properties.getProperty("blockOverlayAlpha"), 70), 10, 180);
            blockOverlayOutlineAlpha = clampInt(parseInt(properties.getProperty("blockOverlayOutlineAlpha"), 190), 40, 255);
        } catch (IOException ignored) { reset(); }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Properties p = new Properties();
            p.setProperty("animations", Boolean.toString(animations)); p.setProperty("hoverEffects", Boolean.toString(hoverEffects));
            p.setProperty("guiScale", Integer.toString(guiScale)); p.setProperty("overlayOpacity", Integer.toString(overlayOpacity));
            p.setProperty("accent", Integer.toString(accent)); p.setProperty("avatarIndex", Integer.toString(avatarIndex));
            p.setProperty("enabledModules", String.join(",", enabledModules));
            p.setProperty("triggerClickMode", Boolean.toString(triggerClickMode)); p.setProperty("triggerCritMode", Boolean.toString(triggerCritMode));
            p.setProperty("triggerMobs", Boolean.toString(triggerMobs)); p.setProperty("triggerAnimals", Boolean.toString(triggerAnimals)); p.setProperty("triggerPlayers", Boolean.toString(triggerPlayers));
            p.setProperty("triggerCps", Integer.toString(triggerCps)); p.setProperty("espPlayers", Boolean.toString(espPlayers)); p.setProperty("espMobs", Boolean.toString(espMobs)); p.setProperty("espAnimals", Boolean.toString(espAnimals)); p.setProperty("espRange", Integer.toString(espRange));
            p.setProperty("blockOverlayAlpha", Integer.toString(blockOverlayAlpha)); p.setProperty("blockOverlayOutlineAlpha", Integer.toString(blockOverlayOutlineAlpha));
            try (OutputStream output = Files.newOutputStream(FILE)) { p.store(output, "TelikinesDLC client configuration"); }
        } catch (IOException ignored) { }
    }

    public static void reset() {
        animations = true; hoverEffects = true; guiScale = 100; overlayOpacity = 54; accent = 0; avatarIndex = 0; enabledModules.clear();
        triggerClickMode = true; triggerCritMode = false; triggerMobs = true; triggerAnimals = false; triggerPlayers = true; triggerCps = 8;
        espPlayers = true; espMobs = true; espAnimals = false; espRange = 64; blockOverlayAlpha = 70; blockOverlayOutlineAlpha = 190; save();
    }
    private static int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return fallback; } }
    private static int clampInt(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static boolean animations() { return animations; } public static void setAnimations(boolean v) { animations=v; save(); }
    public static boolean hoverEffects() { return hoverEffects; } public static void setHoverEffects(boolean v) { hoverEffects=v; save(); }
    public static int guiScale() { return guiScale; } public static void setGuiScale(int v) { guiScale=clampInt(v,80,125); save(); }
    public static int overlayOpacity() { return overlayOpacity; } public static void setOverlayOpacity(int v) { overlayOpacity=clampInt(v,20,85); save(); }
    public static int accent() { return accent; } public static void setAccent(int v) { accent=clampInt(v,0,2); save(); }
    public static int avatarIndex() { return avatarIndex; } public static void setAvatarIndex(int v) { avatarIndex=clampInt(v,0,3); save(); }
    public static boolean isModuleEnabled(String n) { return enabledModules.contains(n); } public static void setModuleEnabled(String n, boolean e) { if(e) enabledModules.add(n); else enabledModules.remove(n); save(); }
    public static boolean triggerClickMode() { return triggerClickMode; } public static boolean triggerCritMode() { return triggerCritMode; } public static boolean triggerMobs() { return triggerMobs; } public static boolean triggerAnimals() { return triggerAnimals; } public static boolean triggerPlayers() { return triggerPlayers; } public static int triggerCps() { return triggerCps; }
    public static void setTriggerClickMode(boolean v){triggerClickMode=v;save();} public static void setTriggerCritMode(boolean v){triggerCritMode=v;save();} public static void setTriggerMobs(boolean v){triggerMobs=v;save();} public static void setTriggerAnimals(boolean v){triggerAnimals=v;save();} public static void setTriggerPlayers(boolean v){triggerPlayers=v;save();} public static void setTriggerCps(int v){triggerCps=clampInt(v,1,20);save();}
    public static boolean espPlayers(){return espPlayers;} public static void setEspPlayers(boolean v){espPlayers=v;save();} public static boolean espMobs(){return espMobs;} public static void setEspMobs(boolean v){espMobs=v;save();} public static boolean espAnimals(){return espAnimals;} public static void setEspAnimals(boolean v){espAnimals=v;save();} public static int espRange(){return espRange;} public static void setEspRange(int v){espRange=clampInt(v,8,128);save();}
    public static int blockOverlayAlpha(){return blockOverlayAlpha;} public static void setBlockOverlayAlpha(int v){blockOverlayAlpha=clampInt(v,10,180);save();}
    public static int blockOverlayOutlineAlpha(){return blockOverlayOutlineAlpha;} public static void setBlockOverlayOutlineAlpha(int v){blockOverlayOutlineAlpha=clampInt(v,40,255);save();}
}
