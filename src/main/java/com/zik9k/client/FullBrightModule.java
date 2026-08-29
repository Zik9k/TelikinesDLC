package com.zik9k.client;

import net.minecraft.client.MinecraftClient;

public final class FullBrightModule extends Module {
    private static final double FULL_GAMMA = 15.0D;
    private Double previousGamma;

    public FullBrightModule() {
        super("Full Bright", "Raises client brightness while enabled", ModuleCategory.RENDER);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            previousGamma = client.options.getGamma().getValue();
            applyGamma(client);
        }
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            applyGamma(client);
        }
    }

    private void applyGamma(MinecraftClient client) {
        client.options.getGamma().setValue(FULL_GAMMA);
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
