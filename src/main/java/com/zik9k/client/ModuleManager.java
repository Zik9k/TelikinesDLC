package com.zik9k.client;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static boolean initialized;

    private ModuleManager() { }

    public static void init() {
        if (initialized) return;
        initialized = true;
        register(new AutoSprintModule());
        register(new FullBrightModule());
        register(new TriggerBotModule());
        register(new ESPModule());
        register(new BlockOverlayModule());
        register(new TracersModule());
        register(new AppleFarmModule());
        register(new KillAuraModule());

        for (Module module : MODULES) {
            if (ClientConfig.isModuleEnabled(module.getName())) module.setEnabled(true);
        }
    }

    private static void register(Module module) { MODULES.add(module); }

    public static void tick(MinecraftClient client) {
        init();
        for (Module module : MODULES) if (module.isEnabled()) module.onTick();
    }

    public static List<Module> getModules() { init(); return Collections.unmodifiableList(MODULES); }

    public static List<Module> getModules(ModuleCategory category) {
        init();
        return MODULES.stream().filter(module -> module.getCategory() == category).toList();
    }
}
