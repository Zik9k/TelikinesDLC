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
    private static final int PANEL_WIDTH = 620;
    private static final int PANEL_HEIGHT = 360;
    private static final int SIDEBAR_WIDTH = 150;
    private static final String[] TABS = {"Combat", "Movement", "Render", "Player", "Misc"};

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
                    client.setScreen(new ClickGuiScreen());
                }
            }
        });
    }

    private static final class ClickGuiScreen extends Screen {
        private int selectedTab;

        private ClickGuiScreen() {
            super(Text.literal("Zik9k Client"));
        }

        @Override
        protected void init() {
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0x99000000);

            int left = (width - PANEL_WIDTH) / 2;
            int top = (height - PANEL_HEIGHT) / 2;
            int right = left + PANEL_WIDTH;
            int bottom = top + PANEL_HEIGHT;

            // Main window.
            context.fill(left + 3, top + 3, right + 3, bottom + 3, 0x55000000);
            context.fill(left, top, right, bottom, 0xFF111111);

            // Header.
            context.fill(left, top, right, top + 48, 0xFF191919);
            context.drawText(textRenderer, title, left + 18, top + 16, 0xFFFFFFFF, false);

            // Sidebar.
            context.fill(left, top + 48, left + SIDEBAR_WIDTH, bottom, 0xFF151515);

            for (int i = 0; i < TABS.length; i++) {
                int tabTop = top + 64 + i * 50;
                boolean selected = i == selectedTab;
                if (selected) {
                    context.fill(left + 10, tabTop - 6, left + SIDEBAR_WIDTH - 10, tabTop + 30, 0xFF2A2A2A);
                }

                context.drawText(
                        textRenderer,
                        Text.literal(TABS[i]),
                        left + 24,
                        tabTop + 5,
                        selected ? 0xFFFFFFFF : 0xFF888888,
                        false
                );
            }

            // Empty content area.
            int contentLeft = left + SIDEBAR_WIDTH;
            context.fill(contentLeft + 1, top + 48, right, bottom, 0xFF101010);
            context.drawText(
                    textRenderer,
                    Text.literal(TABS[selectedTab]),
                    contentLeft + 22,
                    top + 68,
                    0xFFFFFFFF,
                    false
            );
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("Empty"),
                    (contentLeft + right) / 2,
                    (top + bottom) / 2 - 4,
                    0xFF666666
            );

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int left = (width - PANEL_WIDTH) / 2;
                int top = (height - PANEL_HEIGHT) / 2;

                if (mouseX >= left && mouseX <= left + SIDEBAR_WIDTH) {
                    for (int i = 0; i < TABS.length; i++) {
                        int tabTop = top + 64 + i * 50;
                        if (mouseY >= tabTop - 6 && mouseY <= tabTop + 30) {
                            selectedTab = i;
                            return true;
                        }
                    }
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
