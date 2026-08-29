package com.zik9k.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
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

    private static final class EmptyClickGui extends Screen {
        private static final int PANEL_WIDTH = 420;
        private static final int PANEL_HEIGHT = 260;

        private EmptyClickGui() {
            super(Text.literal("Zik9k Client"));
        }

        @Override
        protected void init() {
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, this.width, this.height, 0x99000000);

            int left = (this.width - PANEL_WIDTH) / 2;
            int top = (this.height - PANEL_HEIGHT) / 2;
            int right = left + PANEL_WIDTH;
            int bottom = top + PANEL_HEIGHT;

            context.fill(left + 2, top + 2, right + 2, bottom + 2, 0x55000000);
            context.fill(left, top, right, bottom, 0xFF111111);
            context.fill(left, top, right, top + 36, 0xFF1C1C1C);

            context.drawText(this.textRenderer, this.title, left + 14, top + 12, 0xFFFFFFFF, false);
            context.drawText(this.textRenderer, Text.literal("Empty GUI"), left + 14, top + 52, 0xFFAAAAAA, false);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No modules yet"), this.width / 2, top + 120, 0xFF777777);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
