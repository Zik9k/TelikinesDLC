package com.zik9k.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
    private enum Element { ACTIVE, KEYBINDS, INFO, STAFF }
    private Element dragging;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() { super(Text.literal("HUD Editor")); }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        int accent = ClientConfig.hudColor();
        c.fill(0, 0, width, height, 0x66000000);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("HUD EDITOR"), width / 2, 14, 0xFFF5EFF7);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Перетаскивай блоки ЛКМ • ESC — выйти"), width / 2, 30, 0xFFBEB5C5);

        drawPreview(c, ClientConfig.hudActiveX(), ClientConfig.hudActiveY(), 180, 59, "ACTIVE MODULES", accent);
        drawPreview(c, ClientConfig.hudKeybindX(), ClientConfig.hudKeybindY(), 164, 50, "KEYBINDS", accent);
        int infoY = ClientConfig.hudInfoY() <= 0 ? height - 78 : ClientConfig.hudInfoY();
        drawPreview(c, ClientConfig.hudInfoX(), infoY, 248, 70, "TELIKINESDLC  •  1.21.11", accent);
        drawPreview(c, ClientConfig.hudStaffX(), ClientConfig.hudStaffY(), 202, 63, "STAFF LIST", accent);

        super.render(c, mouseX, mouseY, delta);
    }

    private void drawPreview(DrawContext c, int x, int y, int w, int h, String title, int accent) {
        x = Math.max(0, Math.min(width - w, x));
        y = Math.max(45, Math.min(height - h, y));
        c.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x55000000);
        c.fill(x, y, x + w, y + h, 0xD009070F);
        c.fill(x, y, x + w, y + 2, accent);
        c.fill(x, y + 2, x + 2, y + h, accent);
        c.drawText(textRenderer, Text.literal(title), x + 9, y + 7, 0xFFF5EFF7, true);
        c.drawText(textRenderer, Text.literal("drag me"), x + 9, y + 26, 0xFF9D93A3, false);
        c.drawText(textRenderer, Text.literal("ZIK9K"), x + 9, y + h - 16, accent, false);
    }

    private Element hit(double mx, double my) {
        if (inside(mx, my, ClientConfig.hudActiveX(), ClientConfig.hudActiveY(), 180, 59)) return Element.ACTIVE;
        if (inside(mx, my, ClientConfig.hudKeybindX(), ClientConfig.hudKeybindY(), 164, 50)) return Element.KEYBINDS;
        int infoY = ClientConfig.hudInfoY() <= 0 ? height - 78 : ClientConfig.hudInfoY();
        if (inside(mx, my, ClientConfig.hudInfoX(), infoY, 248, 70)) return Element.INFO;
        if (inside(mx, my, ClientConfig.hudStaffX(), ClientConfig.hudStaffY(), 202, 63)) return Element.STAFF;
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            dragging = hit(click.x(), click.y());
            if (dragging != null) {
                int x = getX(dragging), y = getY(dragging);
                dragOffsetX = (int) click.x() - x;
                dragOffsetY = (int) click.y() - y;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging == null) return super.mouseDragged(click, deltaX, deltaY);
        int x = (int) click.x() - dragOffsetX;
        int y = (int) click.y() - dragOffsetY;
        switch (dragging) {
            case ACTIVE -> ClientConfig.setHudActivePos(x, y);
            case KEYBINDS -> ClientConfig.setHudKeybindPos(x, y);
            case INFO -> ClientConfig.setHudInfoPos(x, y);
            case STAFF -> ClientConfig.setHudStaffPos(x, y);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }

    private int getX(Element e) { return switch (e) { case ACTIVE -> ClientConfig.hudActiveX(); case KEYBINDS -> ClientConfig.hudKeybindX(); case INFO -> ClientConfig.hudInfoX(); case STAFF -> ClientConfig.hudStaffX(); }; }
    private int getY(Element e) { return switch (e) { case ACTIVE -> ClientConfig.hudActiveY(); case KEYBINDS -> ClientConfig.hudKeybindY(); case INFO -> ClientConfig.hudInfoY() <= 0 ? height - 78 : ClientConfig.hudInfoY(); case STAFF -> ClientConfig.hudStaffY(); }; }
    private static boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }

    @Override public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }
    @Override public void close() { if (client != null) client.setScreen(new SettingsScreen()); }
    @Override public boolean shouldPause() { return false; }
}
