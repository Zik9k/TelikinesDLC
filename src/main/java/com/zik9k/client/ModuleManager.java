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
