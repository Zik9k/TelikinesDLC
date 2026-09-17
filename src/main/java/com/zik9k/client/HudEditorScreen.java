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
    private int dragX, dragY;

    public HudEditorScreen() { super(Text.literal("HUD Editor")); }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        int accent = ClientConfig.hudColor();
        c.fill(0, 0, width, height, 0x66000000);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("HUD EDITOR"), width / 2, 14, 0xFFF5EFF7);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("ЛКМ — перетаскивать блок • ESC — выйти"), width / 2, 30, 0xFFBEB5C5);

        drawElement(c, Element.ACTIVE, 180, 59, "ACTIVE MODULES", accent);
        drawElement(c, Element.KEYBINDS, 164, 50, "KEYBINDS", accent);
        drawElement(c, Element.INFO, 248, 70, "TELIKINESDLC  •  1.21.11", accent);
        drawElement(c, Element.STAFF, 202, 63, "STAFF LIST", accent);

        super.render(c, mouseX, mouseY, delta);
    }

    private void drawElement(DrawContext c, Element element, int w, int h, String title, int accent) {
        int x = currentX(element);
        int y = currentY(element);
        x = clampX(x, w);
        y = clampY(y, h);
        boolean selected = dragging == element;

        c.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x55000000);
        c.fill(x, y, x + w, y + h, selected ? 0xE01D1625 : 0xD009070F);
        c.fill(x, y, x + w, y + 2, accent);
        c.fill(x, y + 2, x + 2, y + h, accent);
        if (selected) c.fill(x + w - 2, y + 2, x + w, y + h, accent);
        c.drawText(textRenderer, Text.literal(title), x + 9, y + 7, 0xFFF5EFF7, true);
        c.drawText(textRenderer, Text.literal(selected ? "moving..." : "drag me"), x + 9, y + 26, selected ? 0xFFE6C8F0 : 0xFF9D93A3, false);
        c.drawText(textRenderer, Text.literal("ZIK9K"), x + 9, y + h - 16, accent, false);
    }

    private Element hit(double mx, double my) {
        if (inside(mx, my, currentX(Element.ACTIVE), currentY(Element.ACTIVE), 180, 59)) return Element.ACTIVE;
        if (inside(mx, my, currentX(Element.KEYBINDS), currentY(Element.KEYBINDS), 164, 50)) return Element.KEYBINDS;
        if (inside(mx, my, currentX(Element.INFO), currentY(Element.INFO), 248, 70)) return Element.INFO;
        if (inside(mx, my, currentX(Element.STAFF), currentY(Element.STAFF), 202, 63)) return Element.STAFF;
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            Element hit = hit(click.x(), click.y());
            if (hit != null) {
                dragging = hit;
                dragX = currentX(hit);
                dragY = currentY(hit);
                dragOffsetX = (int) click.x() - dragX;
                dragOffsetY = (int) click.y() - dragY;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging == null || click.button() != 0) return super.mouseDragged(click, deltaX, deltaY);
        dragX = clampX((int) click.x() - dragOffsetX, widthFor(dragging));
        dragY = clampY((int) click.y() - dragOffsetY, heightFor(dragging));
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            savePosition(dragging, dragX, dragY);
            dragging = null;
        }
        return super.mouseReleased(click);
    }

    private int currentX(Element element) {
        if (dragging == element) return dragX;
        return switch (element) {
            case ACTIVE -> ClientConfig.hudActiveX();
            case KEYBINDS -> ClientConfig.hudKeybindX();
            case INFO -> ClientConfig.hudInfoX();
            case STAFF -> ClientConfig.hudStaffX();
        };
    }

    private int currentY(Element element) {
        if (dragging == element) return dragY;
        return switch (element) {
            case ACTIVE -> ClientConfig.hudActiveY();
            case KEYBINDS -> ClientConfig.hudKeybindY();
            case INFO -> ClientConfig.hudInfoY() <= 0 ? height - 78 : ClientConfig.hudInfoY();
            case STAFF -> ClientConfig.hudStaffY();
        };
    }

    private int widthFor(Element element) {
        return switch (element) {
            case ACTIVE -> 180;
            case KEYBINDS -> 164;
            case INFO -> 248;
            case STAFF -> 202;
        };
    }

    private int heightFor(Element element) {
        return switch (element) {
            case ACTIVE -> 59;
            case KEYBINDS -> 50;
            case INFO -> 70;
            case STAFF -> 63;
        };
    }

    private void savePosition(Element element, int x, int y) {
        switch (element) {
            case ACTIVE -> ClientConfig.setHudActivePos(x, y);
            case KEYBINDS -> ClientConfig.setHudKeybindPos(x, y);
            case INFO -> ClientConfig.setHudInfoPos(x, y);
            case STAFF -> ClientConfig.setHudStaffPos(x, y);
        }
    }

    private int clampX(int x, int w) { return Math.max(0, Math.min(width - w, x)); }
    private int clampY(int y, int h) { return Math.max(45, Math.min(height - h, y)); }
    private static boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }

    @Override public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override public void close() { if (client != null) client.setScreen(new SettingsScreen()); }
    @Override public boolean shouldPause() { return false; }
}
