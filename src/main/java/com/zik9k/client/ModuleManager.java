package com.zik9k.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {
    }

    public static void init() {
        if (!MODULES.isEmpty()) {
            return;
        }

        register(new Module("KillAura", "Attack nearby targets", ModuleCategory.COMBAT) { });
        register(new Module("AimAssist", "Assist target rotation", ModuleCategory.COMBAT) { });
        register(new Module("Speed", "Increase movement speed", ModuleCategory.MOVEMENT) { });
        register(new Module("Sprint", "Automatic sprint", ModuleCategory.MOVEMENT) { });
        register(new Module("ESP", "Highlight entities", ModuleCategory.RENDER) { });
        register(new Module("FullBright", "Brighten dark areas", ModuleCategory.RENDER) { });
        register(new Module("AutoArmor", "Equip better armor", ModuleCategory.PLAYER) { });
        register(new Module("NoFall", "Prevent fall damage", ModuleCategory.PLAYER) { });
        register(new Module("ClickGUI", "Open the client menu", ModuleCategory.MISC) { });
        register(new Module("Example", "Placeholder module", ModuleCategory.MISC) { });
    }

    private static void register(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> getModules(ModuleCategory category) {
        return MODULES.stream().filter(module -> module.getCategory() == category).toList();
    }
}
