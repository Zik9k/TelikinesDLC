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
        ModuleManager.init();

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

    public static final class ClickGuiScreen extends Screen {
        private static final int PANEL_WIDTH = 700;
        private static final int PANEL_HEIGHT = 430;
        private static final int SIDEBAR_WIDTH = 150;
        private static final int HEADER_HEIGHT = 52;
        private static final int ROW_HEIGHT = 42;
        private static final int GAP = 8;

        private ModuleCategory selectedCategory = ModuleCategory.COMBAT;

        public ClickGuiScreen() {
            super(Text.literal("Zik9k Client"));
        }

        @Override
        protected void init() {
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0xB5000000);

            int left = (width - PANEL_WIDTH) / 2;
            int top = (height - PANEL_HEIGHT) / 2;
            int right = left + PANEL_WIDTH;
            int bottom = top + PANEL_HEIGHT;

            // Shadow + main panel.
            context.fill(left + 4, top + 5, right + 4, bottom + 5, 0x55000000);
            context.fill(left, top, right, bottom, 0xFF111216);

            // Sidebar.
            context.fill(left, top, left + SIDEBAR_WIDTH, bottom, 0xFF17181D);
            context.fill(left + SIDEBAR_WIDTH, top, right, top + HEADER_HEIGHT, 0xFF1D1E24);

            context.drawText(textRenderer, Text.literal("Zik9k"), left + 18, top + 18, 0xFFFFFFFF, false);
            context.drawText(textRenderer, Text.literal("CLIENT"), left + 77, top + 19, 0xFF7D8290, false);

            int categoryY = top + 64;
            for (ModuleCategory category : ModuleCategory.values()) {
                boolean selected = category == selectedCategory;
                int rowTop = categoryY;
                int rowBottom = rowTop + 34;
                if (selected) {
                    context.fill(left + 10, rowTop, left + SIDEBAR_WIDTH - 10, rowBottom, 0xFF2A2C34);
                    context.fill(left + 10, rowTop, left + 13, rowBottom, 0xFFFFFFFF);
                } else if (mouseX >= left + 10 && mouseX <= left + SIDEBAR_WIDTH - 10
                        && mouseY >= rowTop && mouseY <= rowBottom) {
                    context.fill(left + 10, rowTop, left + SIDEBAR_WIDTH - 10, rowBottom, 0xFF21232A);
                }
                context.drawText(textRenderer, Text.literal(category.getDisplayName()), left + 22, rowTop + 10,
                        selected ? 0xFFFFFFFF : 0xFF9A9DA8, false);
                categoryY += 38;
            }

            int contentLeft = left + SIDEBAR_WIDTH + 20;
            int contentTop = top + HEADER_HEIGHT + 18;
            int contentRight = right - 20;

            context.drawText(textRenderer, Text.literal(selectedCategory.getDisplayName()), contentLeft, top + 19,
                    0xFFFFFFFF, false);
            context.drawText(textRenderer,
                    Text.literal("Modules: " + ModuleManager.getModules(selectedCategory).size()),
                    contentRight - 100, top + 19, 0xFF777C88, false);

            int x = contentLeft;
            int y = contentTop;
            int cardWidth = (contentRight - contentLeft - GAP) / 2;

            var modules = ModuleManager.getModules(selectedCategory);
            for (Module module : modules) {
                int cardRight = x + cardWidth;
                int cardBottom = y + ROW_HEIGHT;
                boolean hovered = mouseX >= x && mouseX <= cardRight && mouseY >= y && mouseY <= cardBottom;
                context.fill(x, y, cardRight, cardBottom, hovered ? 0xFF24262D : 0xFF1B1D23);

                context.drawText(textRenderer, Text.literal(module.getName()), x + 12, y + 8, 0xFFE6E7EB, false);
                context.drawText(textRenderer, Text.literal(module.getDescription()), x + 12, y + 24, 0xFF7E828C, false);

                int toggleRight = cardRight - 12;
                int toggleLeft = toggleRight - 34;
                int toggleTop = y + 11;
                int toggleBottom = toggleTop + 18;
                context.fill(toggleLeft, toggleTop, toggleRight, toggleBottom,
                        module.isEnabled() ? 0xFFFFFFFF : 0xFF34363E);
                if (module.isEnabled()) {
                    context.fill(toggleLeft + 17, toggleTop + 3, toggleRight - 3, toggleBottom - 3, 0xFF111216);
                } else {
                    context.fill(toggleLeft + 3, toggleTop + 3, toggleLeft + 17, toggleBottom - 3, 0xFF8A8E99);
                }

                x += cardWidth + GAP;
                if (x + cardWidth > contentRight + 1) {
                    x = contentLeft;
                    y += ROW_HEIGHT + GAP;
                }
            }

            if (modules.isEmpty()) {
                context.drawText(textRenderer, Text.literal("No modules in this category yet."),
                        contentLeft, contentTop + 18, 0xFF6E727C, false);
            }

            context.drawText(textRenderer, Text.literal("Right Shift"), left + 18, bottom - 34, 0xFF6E727C, false);
            context.drawText(textRenderer, Text.literal("Open / close"), left + 18, bottom - 20, 0xFF51545D, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            int left = (width - PANEL_WIDTH) / 2;
            int top = (height - PANEL_HEIGHT) / 2;
            int right = left + PANEL_WIDTH;
            int sidebarRight = left + SIDEBAR_WIDTH;

            if (mouseX >= left + 10 && mouseX <= sidebarRight - 10) {
                int categoryY = top + 64;
                for (ModuleCategory category : ModuleCategory.values()) {
                    if (mouseY >= categoryY && mouseY <= categoryY + 34) {
                        selectedCategory = category;
                        return true;
                    }
                    categoryY += 38;
                }
            }

            int contentLeft = left + SIDEBAR_WIDTH + 20;
            int contentTop = top + HEADER_HEIGHT + 18;
            int contentRight = right - 20;
            int cardWidth = (contentRight - contentLeft - GAP) / 2;
            int x = contentLeft;
            int y = contentTop;

            for (Module module : ModuleManager.getModules(selectedCategory)) {
                if (mouseX >= x && mouseX <= x + cardWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                    module.toggle();
                    return true;
                }
                x += cardWidth + GAP;
                if (x + cardWidth > contentRight + 1) {
                    x = contentLeft;
                    y += ROW_HEIGHT + GAP;
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
