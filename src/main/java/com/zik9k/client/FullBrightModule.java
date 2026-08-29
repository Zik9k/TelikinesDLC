package com.zik9k.client;

import net.minecraft.client.MinecraftClient;

public final class FullBrightModule extends Module {
    private Double previousGamma;

    public FullBrightModule() {
        super("Full Bright", "Raises client brightness while enabled", ModuleCategory.RENDER);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            previousGamma = client.options.getGamma().getValue();
            client.options.getGamma().setValue(16.0D);
        }
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.getGamma().setValue(16.0D);
        }
    }

    @Override
    protected void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null && previousGamma != null) {
            client.options.getGamma().setValue(previousGamma);
        }
        previousGamma = null;
    }
}
