package com.zik9k.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class Zik9kClient implements ClientModInitializer {
    private static final int PANEL_WIDTH = 680;
    private static final int PANEL_HEIGHT = 400;
    private static final int SIDEBAR_WIDTH = 168;
    private static final String[] TABS = {"Combat", "Render", "Movement", "Player", "Misc"};
    private static final String[] TAB_MARKS = {"+", "o", ">", "*", "#"};
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        ClientConfig.load();
        ModuleManager.init();
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zik9k.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.zik9k"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
        });
    }

    private static final class ClickGuiScreen extends Screen {
        private int selectedTab;
        private int avatarIndex;
        private String searchQuery = "";
        private boolean searchFocused;
        private long lastBlinkTime;
        private boolean cursorVisible = true;

        private ClickGuiScreen() {
            super(Text.literal("TelikinesDLC"));
            avatarIndex = ClientConfig.avatarIndex();
        }

        @Override protected void init() {
            searchFocused = false;
            lastBlinkTime = System.currentTimeMillis();
        }

        private int panelLeft() { return (width - PANEL_WIDTH) / 2; }
        private int panelTop() { return (height - PANEL_HEIGHT) / 2; }
        private int searchTop() { return panelTop() + 13; }
        private int searchBottom() { return panelTop() + 33; }
        private int accentColor() {
            return switch (ClientConfig.accent()) {
                case 1 -> 0xFF8D67FF;
                case 2 -> 0xFFE26BFF;
                default -> 0xFFB15CFF;
            };
        }
        private boolean matches(Module module) {
            if (searchQuery.isBlank()) return true;
            String q = searchQuery.toLowerCase();
            return module.getName().toLowerCase().contains(q) || module.getDescription().toLowerCase().contains(q);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            int overlayAlpha = Math.max(0, Math.min(255, Math.round(ClientConfig.overlayOpacity() * 255f / 100f)));
            context.fill(0, 0, width, height, (overlayAlpha << 24) | 0x08060D);
            int accent = accentColor();
            int left = panelLeft(), top = panelTop(), right = left + PANEL_WIDTH, bottom = top + PANEL_HEIGHT;

            context.fill(left + 5, top + 7, right + 5, bottom + 7, 0x42000000);
            context.fill(left + 2, top, right - 2, bottom, 0xFF17131F);
            context.fill(left, top + 2, right, bottom - 2, 0xFF17131F);
            context.fill(left + 2, top + 2, left + SIDEBAR_WIDTH, bottom - 2, 0xFF17121E);
            context.fill(left + SIDEBAR_WIDTH, top + 46, right - 2, bottom - 2, 0xFF120F18);

            context.drawText(textRenderer, Text.literal("T"), left + 20, top + 16, accent, false);
            context.drawText(textRenderer, Text.literal("TELIKINESDLC"), left + 40, top + 16, 0xFFF4EEF9, false);

            AvatarTextures.draw(context, client, avatarIndex, left + 16, top + 58, 32);
            String username = client.getSession().getUsername();
            if (username.length() > 13) username = username.substring(0, 13);
            context.drawText(textRenderer, Text.literal(username), left + 58, top + 62, 0xFFEFE8F3, false);
            context.drawText(textRenderer, Text.literal("Click avatar to change"), left + 58, top + 76, 0xFF817787, false);

            for (int i = 0; i < TABS.length; i++) {
                int tabTop = top + 112 + i * 48;
                boolean selected = i == selectedTab;
                if (selected) {
                    context.fill(left + 10, tabTop - 8, left + SIDEBAR_WIDTH - 10, tabTop + 28, 0xFF2C2038);
                    context.fill(left + 10, tabTop - 8, left + 13, tabTop + 28, accent);
                }
                context.drawText(textRenderer, Text.literal(TAB_MARKS[i]), left + 25, tabTop + 3, selected ? accent : 0xFF776D7D, false);
                context.drawText(textRenderer, Text.literal(TABS[i]), left + 49, tabTop + 3, selected ? 0xFFF7F1FA : 0xFF8F8794, false);
            }

            int settingsTop = bottom - 47;
            boolean settingsHovered = mouseX >= left + 10 && mouseX <= left + SIDEBAR_WIDTH - 10 && mouseY >= settingsTop - 6 && mouseY <= settingsTop + 24;
            if (settingsHovered) context.fill(left + 10, settingsTop - 6, left + SIDEBAR_WIDTH - 10, settingsTop + 24, 0xFF28202F);
            context.drawText(textRenderer, Text.literal("S"), left + 25, settingsTop + 1, settingsHovered ? accent : 0xFF776D7D, false);
            context.drawText(textRenderer, Text.literal("Settings"), left + 49, settingsTop + 1, settingsHovered ? 0xFFF7F1FA : 0xFF8F8794, false);

            int contentLeft = left + SIDEBAR_WIDTH + 1;
            context.fill(contentLeft, top + 2, right - 2, top + 46, 0xFF1A1622);
            context.fill(contentLeft + 18, top + 13, contentLeft + 104, top + 33, 0xFF24202B);
            context.drawText(textRenderer, Text.literal("1.21.11"), contentLeft + 27, top + 18, 0xFFBBB2C2, false);
            context.drawText(textRenderer, Text.literal("v"), contentLeft + 88, top + 18, 0xFF766C7B, false);

            int searchRight = right - 20, searchLeft = searchRight - 190;
            boolean hovered = mouseX >= searchLeft && mouseX <= searchRight && mouseY >= searchTop() && mouseY <= searchBottom();
            context.fill(searchLeft, searchTop(), searchRight, searchBottom(), searchFocused ? 0xFF2B2634 : hovered ? 0xFF292430 : 0xFF24202B);
            String visible = searchQuery.length() > 24 ? searchQuery.substring(0, 24) : searchQuery;
            context.drawText(textRenderer, Text.literal(visible.isEmpty() ? "Search" : visible), searchLeft + 11, top + 18, visible.isEmpty() ? 0xFF706775 : 0xFFD9D0DD, false);
            context.drawText(textRenderer, Text.literal("/"), searchRight - 18, top + 18, 0xFF706775, false);
            if (searchFocused) {
                if (System.currentTimeMillis() - lastBlinkTime > 500) { cursorVisible = !cursorVisible; lastBlinkTime = System.currentTimeMillis(); }
                if (cursorVisible) { int cx = searchLeft + 11 + textRenderer.getWidth(visible) + 1; context.fill(cx, top + 17, cx + 1, top + 30, 0xFFD9D0DD); }
            }

            context.drawText(textRenderer, Text.literal(TABS[selectedTab]), contentLeft + 24, top + 68, 0xFFF4EDF8, false);
            List<Module> modules = ModuleManager.getModules(ModuleCategory.values()[selectedTab]).stream().filter(this::matches).toList();
            if (modules.isEmpty()) {
                context.drawText(textRenderer, Text.literal(searchQuery.isBlank() ? "No modules in this category" : "No matching modules"), contentLeft + 24, top + 91, 0xFF6F6673, false);
            } else {
                int cardY = top + 104;
                for (Module module : modules) {
                    if (cardY > bottom - 28) break;
                    drawModuleCard(context, module, contentLeft + 22, cardY, right - 20, accent, mouseX, mouseY);
                    cardY += 58;
                }
            }
            super.render(context, mouseX, mouseY, delta);
        }

        private void drawModuleCard(DrawContext context, Module module, int x, int y, int right, int accent, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= right && mouseY >= y && mouseY <= y + 48;
            context.fill(x, y, right, y + 48, hovered ? 0xFF211B28 : 0xFF1B1720);
            if (module.isEnabled()) context.fill(x, y, x + 3, y + 48, accent);
            context.drawText(textRenderer, Text.literal(module.getName()), x + 12, y + 8, module.isEnabled() ? 0xFFF5EDF9 : 0xFFD8CFDD, false);
            String description = module.getDescription();
            if (description.length() > 43) description = description.substring(0, 43) + "...";
            context.drawText(textRenderer, Text.literal(description), x + 12, y + 25, 0xFF817987, false);
            context.fill(right - 58, y + 13, right - 12, y + 34, module.isEnabled() ? 0xFF49305B : 0xFF25202A);
            context.drawText(textRenderer, Text.literal(module.isEnabled() ? "ON" : "OFF"), right - 46, y + 18, module.isEnabled() ? 0xFFE5D2EF : 0xFF7E7584, false);
        }

        @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
            int left = panelLeft(), top = panelTop(), right = left + PANEL_WIDTH, bottom = top + PANEL_HEIGHT;
            if (mouseX >= left + 10 && mouseX <= left + SIDEBAR_WIDTH - 10) {
                for (int i = 0; i < TABS.length; i++) {
                    int tabTop = top + 112 + i * 48;
                    if (mouseY >= tabTop - 8 && mouseY <= tabTop + 28) { selectedTab = i; searchFocused = false; return true; }
                }
                int settingsTop = bottom - 47;
                if (mouseY >= settingsTop - 6 && mouseY <= settingsTop + 24) { client.setScreen(new SettingsScreen()); return true; }
            }
            if (mouseX >= left + 12 && mouseX <= left + 52 && mouseY >= top + 54 && mouseY <= top + 94) {
                avatarIndex = (avatarIndex + 1) % AvatarTextures.count(); ClientConfig.setAvatarIndex(avatarIndex); return true;
            }
            int searchLeft = right - 210, searchRight = right - 20;
            if (mouseX >= searchLeft && mouseX <= searchRight && mouseY >= top + 13 && mouseY <= top + 33) {
                searchFocused = true; lastBlinkTime = System.currentTimeMillis(); cursorVisible = true; return true;
            }
            List<Module> modules = ModuleManager.getModules(ModuleCategory.values()[selectedTab]).stream().filter(this::matches).toList();
            int cardY = top + 104, cardX = left + SIDEBAR_WIDTH + 23, cardRight = right - 20;
            for (Module module : modules) {
                if (cardY > bottom - 28) break;
                if (mouseX >= cardX && mouseX <= cardRight && mouseY >= cardY && mouseY <= cardY + 48) { module.toggle(); return true; }
                cardY += 58;
            }
            searchFocused = false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override public boolean charTyped(char chr, int modifiers) {
            if (searchFocused && chr >= 32 && chr != 127 && searchQuery.length() < 48) { searchQuery += chr; lastBlinkTime = System.currentTimeMillis(); cursorVisible = true; return true; }
            return super.charTyped(chr, modifiers);
        }

        @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (searchFocused) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) { if (!searchQuery.isEmpty()) searchQuery = searchQuery.substring(0, searchQuery.length() - 1); lastBlinkTime = System.currentTimeMillis(); cursorVisible = true; return true; }
                if (keyCode == GLFW.GLFW_KEY_DELETE) { searchQuery = ""; lastBlinkTime = System.currentTimeMillis(); cursorVisible = true; return true; }
                if (keyCode == GLFW.GLFW_KEY_A && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) { searchQuery = ""; return true; }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override public boolean shouldPause() { return false; }
    }
}
