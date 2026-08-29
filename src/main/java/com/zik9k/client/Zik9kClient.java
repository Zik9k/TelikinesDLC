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
    private static final int PANEL_WIDTH = 680;
    private static final int PANEL_HEIGHT = 400;
    private static final int SIDEBAR_WIDTH = 168;
    private static final String[] TABS = {"Combat", "Render", "Movement", "Player", "Misc"};
    private static final String[] TAB_MARKS = {"+", "o", ">", "*", "#"};

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
        private String searchQuery = "";
        private boolean searchFocused;
        private long lastBlinkTime;
        private boolean cursorVisible = true;

        private ClickGuiScreen() {
            super(Text.literal("TelikinesDLC"));
        }

        @Override
        protected void init() {
            searchFocused = false;
            lastBlinkTime = System.currentTimeMillis();
        }

        private int panelLeft() {
            return (width - PANEL_WIDTH) / 2;
        }

        private int panelTop() {
            return (height - PANEL_HEIGHT) / 2;
        }

        private int searchLeft() {
            return panelLeft() + SIDEBAR_WIDTH + 1 + 680 - SIDEBAR_WIDTH - 20 - 190;
        }

        private int searchRight() {
            return panelLeft() + PANEL_WIDTH - 20;
        }

        private int searchTop() {
            return panelTop() + 13;
        }

        private int searchBottom() {
            return panelTop() + 33;
        }

        private boolean isSearchMatch(String name, String description) {
            if (searchQuery.isBlank()) {
                return true;
            }
            String query = searchQuery.toLowerCase();
            return name.toLowerCase().contains(query) || description.toLowerCase().contains(query);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0x8A08060D);

            int left = panelLeft();
            int top = panelTop();
            int right = left + PANEL_WIDTH;
            int bottom = top + PANEL_HEIGHT;

            context.fill(left + 5, top + 7, right + 5, bottom + 7, 0x42000000);
            context.fill(left + 2, top, right - 2, bottom, 0xFF17131F);
            context.fill(left, top + 2, right, bottom - 2, 0xFF17131F);

            context.fill(left + 2, top + 2, left + SIDEBAR_WIDTH, bottom - 2, 0xFF17121E);
            context.fill(left + SIDEBAR_WIDTH, top + 46, right - 2, bottom - 2, 0xFF120F18);

            context.drawText(textRenderer, Text.literal("T"), left + 20, top + 16, 0xFFB26BFF, false);
            context.drawText(textRenderer, Text.literal("TELIKINESDLC"), left + 40, top + 16, 0xFFF4EEF9, false);

            context.fill(left + 16, top + 58, left + 48, top + 90, 0xFF3A3045);
            context.drawText(textRenderer, Text.literal("T"), left + 28, top + 68, 0xFFD5B8FF, false);
            context.drawText(textRenderer, Text.literal("Client"), left + 58, top + 62, 0xFFEFE8F3, false);
            context.drawText(textRenderer, Text.literal("Minecraft 1.21.11"), left + 58, top + 76, 0xFF817787, false);

            for (int i = 0; i < TABS.length; i++) {
                int tabTop = top + 112 + i * 48;
                boolean selected = i == selectedTab;

                if (selected) {
                    context.fill(left + 10, tabTop - 8, left + SIDEBAR_WIDTH - 10, tabTop + 28, 0xFF2C2038);
                    context.fill(left + 10, tabTop - 8, left + 13, tabTop + 28, 0xFFB15CFF);
                }

                int markColor = selected ? 0xFFB96CFF : 0xFF776D7D;
                int textColor = selected ? 0xFFF7F1FA : 0xFF8F8794;

                context.drawText(textRenderer, Text.literal(TAB_MARKS[i]), left + 25, tabTop + 3, markColor, false);
                context.drawText(textRenderer, Text.literal(TABS[i]), left + 49, tabTop + 3, textColor, false);
            }

            int contentLeft = left + SIDEBAR_WIDTH + 1;
            context.fill(contentLeft, top + 2, right - 2, top + 46, 0xFF1A1622);

            context.fill(contentLeft + 18, top + 13, contentLeft + 104, top + 33, 0xFF24202B);
            context.drawText(textRenderer, Text.literal("1.21.11"), contentLeft + 27, top + 18, 0xFFBBB2C2, false);
            context.drawText(textRenderer, Text.literal("v"), contentLeft + 88, top + 18, 0xFF766C7B, false);

            int searchRight = right - 20;
            int searchLeft = searchRight - 190;
            boolean hasQuery = !searchQuery.isEmpty();
            boolean hovered = mouseX >= searchLeft && mouseX <= searchRight && mouseY >= searchTop() && mouseY <= searchBottom();
            int searchBg = searchFocused ? 0xFF2B2634 : hovered ? 0xFF292430 : 0xFF24202B;
            context.fill(searchLeft, searchTop(), searchRight, searchBottom(), searchBg);

            if (hasQuery) {
                String visible = searchQuery.length() > 24 ? searchQuery.substring(0, 24) : searchQuery;
                context.drawText(textRenderer, Text.literal(visible), searchLeft + 11, top + 18, 0xFFD9D0DD, false);
                if (searchFocused && System.currentTimeMillis() - lastBlinkTime > 500) {
                    cursorVisible = !cursorVisible;
                    lastBlinkTime = System.currentTimeMillis();
                }
                if (searchFocused && cursorVisible) {
                    int cursorX = searchLeft + 11 + textRenderer.getWidth(visible) + 1;
                    context.fill(cursorX, top + 17, cursorX + 1, top + 30, 0xFFD9D0DD);
                }
            } else if (searchFocused) {
                context.drawText(textRenderer, Text.literal("Search modules..."), searchLeft + 11, top + 18, 0xFF7D7383, false);
                if (System.currentTimeMillis() - lastBlinkTime > 500) {
                    cursorVisible = !cursorVisible;
                    lastBlinkTime = System.currentTimeMillis();
                }
                if (cursorVisible) {
                    context.fill(searchLeft + 11, top + 17, searchLeft + 12, top + 30, 0xFFD9D0DD);
                }
            } else {
                context.drawText(textRenderer, Text.literal("Search"), searchLeft + 11, top + 18, 0xFF706775, false);
            }
            context.drawText(textRenderer, Text.literal("/"), searchRight - 18, top + 18, 0xFF706775, false);

            context.drawText(textRenderer, Text.literal(TABS[selectedTab]), contentLeft + 24, top + 68, 0xFFF4EDF8, false);
            if (searchQuery.isBlank()) {
                context.drawText(textRenderer, Text.literal("Empty"), contentLeft + 24, top + 88, 0xFF6F6673, false);
            } else {
                context.drawText(textRenderer, Text.literal("Search: " + searchQuery), contentLeft + 24, top + 88, 0xFF8D8291, false);
                context.drawCenteredTextWithShadow(
                        textRenderer,
                        Text.literal("No matching modules"),
                        (contentLeft + right) / 2,
                        (top + bottom) / 2 + 11,
                        0xFF5E5663
                );
            }

            if (searchQuery.isBlank()) {
                int cx = (contentLeft + right) / 2;
                int cy = (top + bottom) / 2 + 15;
                context.fill(cx - 28, cy - 28, cx + 28, cy + 28, 0xFF1B1720);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("—"), cx, cy - 4, 0xFF5E5663);
            }

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int left = panelLeft();
            int top = panelTop();
            int right = left + PANEL_WIDTH;

            if (mouseX >= left + 10 && mouseX <= left + SIDEBAR_WIDTH - 10) {
                for (int i = 0; i < TABS.length; i++) {
                    int tabTop = top + 112 + i * 48;
                    if (mouseY >= tabTop - 8 && mouseY <= tabTop + 28) {
                        selectedTab = i;
                        return true;
                    }
                }
            }

            int searchLeft = right - 210;
            int searchRight = right - 20;
            if (mouseX >= searchLeft && mouseX <= searchRight && mouseY >= top + 13 && mouseY <= top + 33) {
                searchFocused = true;
                lastBlinkTime = System.currentTimeMillis();
                cursorVisible = true;
                return true;
            }

            searchFocused = false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (searchFocused && chr >= 32 && chr != 127 && searchQuery.length() < 48) {
                searchQuery += chr;
                lastBlinkTime = System.currentTimeMillis();
                cursorVisible = true;
                return true;
            }
            return super.charTyped(chr, modifiers);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (searchFocused) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    searchFocused = false;
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    if (!searchQuery.isEmpty()) {
                        searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    }
                    lastBlinkTime = System.currentTimeMillis();
                    cursorVisible = true;
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_DELETE) {
                    searchQuery = "";
                    lastBlinkTime = System.currentTimeMillis();
                    cursorVisible = true;
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_A && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}
