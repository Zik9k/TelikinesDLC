package com.zik9k.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class Zik9kClient implements ClientModInitializer {
    public static final String MOD_ID = "zik9k-client";
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zik9k.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.zik9k"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new EmptyClickGui());
                }
            }
        });
    }

    private static final class EmptyClickGui extends net.minecraft.client.gui.screen.Screen {
        private EmptyClickGui() {
            super(net.minecraft.text.Text.literal("Zik9k Client"));
        }

        @Override
        protected void init() {
            // Intentionally empty: this is the first blank GUI iteration.
        }

        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            // Minimal blank background. No modules/components yet.
            context.fill(0, 0, this.width, this.height, 0x99000000);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
