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
    private static boolean animations = true, hoverEffects = true;
    private static int guiScale = 100, overlayOpacity = 54, accent = 0, avatarIndex = 0;
    private static int hudColor = 0xFFB15CFF;
    private static int hudActiveX = 8, hudActiveY = 8;
    private static int hudKeybindX = 8, hudKeybindY = 8;
    private static int hudInfoX = 8, hudInfoY = 0;
    private static int hudStaffX = 8, hudStaffY = 62;
    private static String plan = "FREE";
    private static final Set<String> enabledModules = new LinkedHashSet<>();
    private static boolean triggerClickMode = true, triggerCritMode, triggerMobs = true, triggerAnimals, triggerPlayers = true;
    private static int triggerCps = 8;
    private static boolean espPlayers = true, espMobs = true, espAnimals;
    private static int espRange = 64;
    private static int blockOverlayAlpha = 70, blockOverlayOutlineAlpha = 190;
    private static boolean tracersPlayers = true, tracersMobs = true, tracersAnimals;
    private static int tracersRange = 64;
    private static boolean killAuraPlayers = true, killAuraMobs = true, killAuraAnimals;
    private static int killAuraRange = 4, killAuraCps = 8;

    private ClientConfig() { }

    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(FILE)) { p.load(in); }
            animations = Boolean.parseBoolean(p.getProperty("animations", "true"));
            hoverEffects = Boolean.parseBoolean(p.getProperty("hoverEffects", "true"));
            guiScale = clampInt(parseInt(p.getProperty("guiScale"), 100), 80, 125);
            overlayOpacity = clampInt(parseInt(p.getProperty("overlayOpacity"), 54), 20, 85);
            accent = clampInt(parseInt(p.getProperty("accent"), 0), 0, 4);
            avatarIndex = clampInt(parseInt(p.getProperty("avatarIndex"), 0), 0, 3);
            hudColor = parseInt(p.getProperty("hudColor"), 0xFFB15CFF);
            hudActiveX = clampInt(parseInt(p.getProperty("hudActiveX"), 8), 0, 3000);
            hudActiveY = clampInt(parseInt(p.getProperty("hudActiveY"), 8), 0, 3000);
            hudKeybindX = clampInt(parseInt(p.getProperty("hudKeybindX"), 8), 0, 3000);
            hudKeybindY = clampInt(parseInt(p.getProperty("hudKeybindY"), 8), 0, 3000);
            hudInfoX = clampInt(parseInt(p.getProperty("hudInfoX"), 8), 0, 3000);
            hudInfoY = clampInt(parseInt(p.getProperty("hudInfoY"), 0), 0, 3000);
            hudStaffX = clampInt(parseInt(p.getProperty("hudStaffX"), 8), 0, 3000);
            hudStaffY = clampInt(parseInt(p.getProperty("hudStaffY"), 62), 0, 3000);
            plan = p.getProperty("plan", "FREE").equalsIgnoreCase("PLUS") ? "PLUS" : "FREE";
            enabledModules.clear();
            String stored = p.getProperty("enabledModules", "");
            if (!stored.isBlank()) Arrays.stream(stored.split(",")).map(String::trim).filter(n -> !n.isEmpty()).forEach(enabledModules::add);
            triggerClickMode = Boolean.parseBoolean(p.getProperty("triggerClickMode", "true"));
            triggerCritMode = Boolean.parseBoolean(p.getProperty("triggerCritMode", "false"));
            triggerMobs = Boolean.parseBoolean(p.getProperty("triggerMobs", "true"));
            triggerAnimals = Boolean.parseBoolean(p.getProperty("triggerAnimals", "false"));
            triggerPlayers = Boolean.parseBoolean(p.getProperty("triggerPlayers", "true"));
            triggerCps = clampInt(parseInt(p.getProperty("triggerCps"), 8), 1, 20);
            espPlayers = Boolean.parseBoolean(p.getProperty("espPlayers", "true"));
            espMobs = Boolean.parseBoolean(p.getProperty("espMobs", "true"));
            espAnimals = Boolean.parseBoolean(p.getProperty("espAnimals", "false"));
            espRange = clampInt(parseInt(p.getProperty("espRange"), 64), 8, 128);
            blockOverlayAlpha = clampInt(parseInt(p.getProperty("blockOverlayAlpha"), 70), 10, 180);
            blockOverlayOutlineAlpha = clampInt(parseInt(p.getProperty("blockOverlayOutlineAlpha"), 190), 40, 255);
            tracersPlayers = Boolean.parseBoolean(p.getProperty("tracersPlayers", "true"));
            tracersMobs = Boolean.parseBoolean(p.getProperty("tracersMobs", "true"));
            tracersAnimals = Boolean.parseBoolean(p.getProperty("tracersAnimals", "false"));
            tracersRange = clampInt(parseInt(p.getProperty("tracersRange"), 64), 8, 128);
            killAuraPlayers = Boolean.parseBoolean(p.getProperty("killAuraPlayers", "true"));
            killAuraMobs = Boolean.parseBoolean(p.getProperty("killAuraMobs", "true"));
            killAuraAnimals = Boolean.parseBoolean(p.getProperty("killAuraAnimals", "false"));
            killAuraRange = clampInt(parseInt(p.getProperty("killAuraRange"), 4), 3, 6);
            killAuraCps = clampInt(parseInt(p.getProperty("killAuraCps"), 8), 1, 20);
        } catch (IOException ignored) { reset(); }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Properties p = new Properties();
            p.setProperty("animations", Boolean.toString(animations)); p.setProperty("hoverEffects", Boolean.toString(hoverEffects));
            p.setProperty("guiScale", Integer.toString(guiScale)); p.setProperty("overlayOpacity", Integer.toString(overlayOpacity));
            p.setProperty("accent", Integer.toString(accent)); p.setProperty("avatarIndex", Integer.toString(avatarIndex));
            p.setProperty("hudColor", Integer.toString(hudColor));
            p.setProperty("hudActiveX", Integer.toString(hudActiveX)); p.setProperty("hudActiveY", Integer.toString(hudActiveY));
            p.setProperty("hudKeybindX", Integer.toString(hudKeybindX)); p.setProperty("hudKeybindY", Integer.toString(hudKeybindY));
            p.setProperty("hudInfoX", Integer.toString(hudInfoX)); p.setProperty("hudInfoY", Integer.toString(hudInfoY));
            p.setProperty("hudStaffX", Integer.toString(hudStaffX)); p.setProperty("hudStaffY", Integer.toString(hudStaffY));
            p.setProperty("plan", plan);
            p.setProperty("enabledModules", String.join(",", enabledModules));
            p.setProperty("triggerClickMode", Boolean.toString(triggerClickMode)); p.setProperty("triggerCritMode", Boolean.toString(triggerCritMode));
            p.setProperty("triggerMobs", Boolean.toString(triggerMobs)); p.setProperty("triggerAnimals", Boolean.toString(triggerAnimals)); p.setProperty("triggerPlayers", Boolean.toString(triggerPlayers));
            p.setProperty("triggerCps", Integer.toString(triggerCps));
            p.setProperty("espPlayers", Boolean.toString(espPlayers)); p.setProperty("espMobs", Boolean.toString(espMobs)); p.setProperty("espAnimals", Boolean.toString(espAnimals)); p.setProperty("espRange", Integer.toString(espRange));
            p.setProperty("blockOverlayAlpha", Integer.toString(blockOverlayAlpha)); p.setProperty("blockOverlayOutlineAlpha", Integer.toString(blockOverlayOutlineAlpha));
            p.setProperty("tracersPlayers", Boolean.toString(tracersPlayers)); p.setProperty("tracersMobs", Boolean.toString(tracersMobs)); p.setProperty("tracersAnimals", Boolean.toString(tracersAnimals)); p.setProperty("tracersRange", Integer.toString(tracersRange));
            p.setProperty("killAuraPlayers", Boolean.toString(killAuraPlayers)); p.setProperty("killAuraMobs", Boolean.toString(killAuraMobs)); p.setProperty("killAuraAnimals", Boolean.toString(killAuraAnimals));
            p.setProperty("killAuraRange", Integer.toString(killAuraRange)); p.setProperty("killAuraCps", Integer.toString(killAuraCps));
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "TelikinesDLC client configuration"); }
        } catch (IOException ignored) { }
    }

    public static void reset() {
        animations = true; hoverEffects = true; guiScale = 100; overlayOpacity = 54; accent = 0; avatarIndex = 0;
        hudColor = 0xFFB15CFF; hudActiveX = 8; hudActiveY = 8; hudKeybindX = 8; hudKeybindY = 8; hudInfoX = 8; hudInfoY = 0; hudStaffX = 8; hudStaffY = 62; plan = "FREE"; enabledModules.clear();
        triggerClickMode = true; triggerCritMode = false; triggerMobs = true; triggerAnimals = false; triggerPlayers = true; triggerCps = 8;
        espPlayers = true; espMobs = true; espAnimals = false; espRange = 64;
        blockOverlayAlpha = 70; blockOverlayOutlineAlpha = 190;
        tracersPlayers = true; tracersMobs = true; tracersAnimals = false; tracersRange = 64;
        killAuraPlayers = true; killAuraMobs = true; killAuraAnimals = false; killAuraRange = 4; killAuraCps = 8; save();
    }
    private static int parseInt(String value, int fallback) { try { return Integer.decode(value); } catch (NumberFormatException ignored) { return fallback; } }
    private static int clampInt(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static boolean animations() { return animations; } public static void setAnimations(boolean v) { animations=v; save(); }
    public static boolean hoverEffects() { return hoverEffects; } public static void setHoverEffects(boolean v) { hoverEffects=v; save(); }
    public static int guiScale() { return guiScale; } public static void setGuiScale(int v) { guiScale=clampInt(v,80,125); save(); }
    public static int overlayOpacity() { return overlayOpacity; } public static void setOverlayOpacity(int v) { overlayOpacity=clampInt(v,20,85); save(); }
    public static int accent() { return accent; } public static void setAccent(int v) { accent=clampInt(v,0,4); hudColor = accentColor(v); save(); }
    public static int hudColor() { return hudColor; } public static void setHudColor(int color) { hudColor = color | 0xFF000000; save(); }
    public static int accentColor(int value) { return switch (value) { case 1 -> 0xFF8D67FF; case 2 -> 0xFFE26BFF; case 3 -> 0xFF36D7FF; case 4 -> 0xFFFF5C91; default -> 0xFFB15CFF; }; }
    public static int avatarIndex() { return avatarIndex; } public static void setAvatarIndex(int v) { avatarIndex=clampInt(v,0,3); save(); }
    public static int hudActiveX(){return hudActiveX;} public static int hudActiveY(){return hudActiveY;} public static void setHudActivePos(int x,int y){hudActiveX=Math.max(0,x);hudActiveY=Math.max(0,y);save();}
    public static int hudKeybindX(){return hudKeybindX;} public static int hudKeybindY(){return hudKeybindY;} public static void setHudKeybindPos(int x,int y){hudKeybindX=Math.max(0,x);hudKeybindY=Math.max(0,y);save();}
    public static int hudInfoX(){return hudInfoX;} public static int hudInfoY(){return hudInfoY;} public static void setHudInfoPos(int x,int y){hudInfoX=Math.max(0,x);hudInfoY=Math.max(0,y);save();}
    public static int hudStaffX(){return hudStaffX;} public static int hudStaffY(){return hudStaffY;} public static void setHudStaffPos(int x,int y){hudStaffX=Math.max(0,x);hudStaffY=Math.max(0,y);save();}
    public static boolean isPlus(){return "PLUS".equals(plan);} public static String plan(){return plan;} public static void setPlan(String value){plan="PLUS".equalsIgnoreCase(value)?"PLUS":"FREE";save();}
    public static boolean isModuleEnabled(String n) { return enabledModules.contains(n); } public static void setModuleEnabled(String n, boolean e) { if(e) enabledModules.add(n); else enabledModules.remove(n); save(); }
    public static boolean triggerClickMode() { return triggerClickMode; } public static boolean triggerCritMode() { return triggerCritMode; } public static boolean triggerMobs() { return triggerMobs; } public static boolean triggerAnimals() { return triggerAnimals; } public static boolean triggerPlayers() { return triggerPlayers; } public static int triggerCps() { return triggerCps; }
    public static void setTriggerClickMode(boolean v){triggerClickMode=v;save();} public static void setTriggerCritMode(boolean v){triggerCritMode=v;save();} public static void setTriggerMobs(boolean v){triggerMobs=v;save();} public static void setTriggerAnimals(boolean v){triggerAnimals=v;save();} public static void setTriggerPlayers(boolean v){triggerPlayers=v;save();} public static void setTriggerCps(int v){triggerCps=clampInt(v,1,20);save();}
    public static boolean espPlayers(){return espPlayers;} public static void setEspPlayers(boolean v){espPlayers=v;save();} public static boolean espMobs(){return espMobs;} public static void setEspMobs(boolean v){espMobs=v;save();} public static boolean espAnimals(){return espAnimals;} public static void setEspAnimals(boolean v){espAnimals=v;save();} public static int espRange(){return espRange;} public static void setEspRange(int v){espRange=clampInt(v,8,128);save();}
    public static int blockOverlayAlpha(){return blockOverlayAlpha;} public static void setBlockOverlayAlpha(int v){blockOverlayAlpha=clampInt(v,10,180);save();}
    public static int blockOverlayOutlineAlpha(){return blockOverlayOutlineAlpha;} public static void setBlockOverlayOutlineAlpha(int v){blockOverlayOutlineAlpha=clampInt(v,40,255);save();}
    public static boolean tracersPlayers(){return tracersPlayers;} public static void setTracersPlayers(boolean v){tracersPlayers=v;save();}
    public static boolean tracersMobs(){return tracersMobs;} public static void setTracersMobs(boolean v){tracersMobs=v;save();}
    public static boolean tracersAnimals(){return tracersAnimals;} public static void setTracersAnimals(boolean v){tracersAnimals=v;save();}
    public static int tracersRange(){return tracersRange;} public static void setTracersRange(int v){tracersRange=clampInt(v,8,128);save();}
    public static boolean killAuraPlayers(){return killAuraPlayers;} public static void setKillAuraPlayers(boolean v){killAuraPlayers=v;save();}
    public static boolean killAuraMobs(){return killAuraMobs;} public static void setKillAuraMobs(boolean v){killAuraMobs=v;save();}
    public static boolean killAuraAnimals(){return killAuraAnimals;} public static void setKillAuraAnimals(boolean v){killAuraAnimals=v;save();}
    public static int killAuraRange(){return killAuraRange;} public static void setKillAuraRange(int v){killAuraRange=clampInt(v,3,6);save();}
    public static int killAuraCps(){return killAuraCps;} public static void setKillAuraCps(int v){killAuraCps=clampInt(v,1,20);save();}
}
