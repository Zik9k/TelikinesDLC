package com.zik9k.client;

public abstract class Module {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;

    protected Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
        ClientConfig.setModuleEnabled(name, enabled);
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public void onTick() {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public final String getName() { return name; }
    public final String getDescription() { return description; }
    public final ModuleCategory getCategory() { return category; }
    public final boolean isEnabled() { return enabled; }
}
