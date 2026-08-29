package com.zik9k.client;

import net.minecraft.client.MinecraftClient;

public final class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super("Auto Sprint", "Automatically sprints while moving forward", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean shouldSprint = client.options.forwardKey.isPressed()
                && !client.options.sneakKey.isPressed()
                && !client.player.isUsingItem()
                && !client.player.isTouchingWater();

        client.player.setSprinting(shouldSprint);
    }
}
