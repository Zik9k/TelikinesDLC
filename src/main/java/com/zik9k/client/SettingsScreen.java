package com.zik9k.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class SettingsScreen extends Screen {
    private static final int WIDTH = 620;
    private static final int HEIGHT = 430;
    private boolean resetConfirm;

    public SettingsScreen() { super(Text.literal("TelikinesDLC Settings")); }

    @Override protected void init() { }
    private int left() { return (width - WIDTH) / 2; }
    private int top() { return (height - HEIGHT) / 2; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int left = left(), top = top(), right = left + WIDTH, bottom = top + HEIGHT;
        int accent = ClientConfig.hudColor();
        context.fill(0, 0, width, height, 0x8A08060D);
        context.fill(left + 5, top + 7, right + 5, bottom + 7, 0x42000000);
        context.fill(left + 2, top, right - 2, bottom, 0xFF17131F);
        context.fill(left, top + 2, right, bottom - 2, 0xFF17131F);
        context.fill(left + 2, top + 2, right - 2, top + 50, 0xFF1A1622);
        context.fill(left + 2, top + 2, left + 6, bottom - 2, accent);

        context.drawText(textRenderer, Text.literal("SETTINGS"), left + 20, top + 17, 0xFFF4EEF9, true);
        context.drawText(textRenderer, Text.literal("TelikinesDLC configuration"), left + 110, top + 18, 0xFF776E7E, false);

        drawToggle(context, left + 22, top + 68, "Animations", ClientConfig.animations(), mouseX, mouseY);
        drawToggle(context, left + 22, top + 108, "Hover effects", ClientConfig.hoverEffects(), mouseX, mouseY);

        drawSlider(context, left + 22, top + 157, "GUI scale", ClientConfig.guiScale(), 80, 125, "%d%%", mouseX, mouseY);
        drawSlider(context, left + 22, top + 207, "Overlay opacity", ClientConfig.overlayOpacity(), 20, 85, "%d%%", mouseX, mouseY);

        context.drawText(textRenderer, Text.literal("HUD COLOR"), left + 22, top + 258, 0xFFD8CFDD, false);
        int[] colors = {0xFFB15CFF, 0xFF8D67FF, 0xFFE26BFF, 0xFF36D7FF, 0xFFFF5C91};
        String[] names = {"Purple", "Violet", "Pink", "Cyan", "Rose"};
        for (int i = 0; i < colors.length; i++) {
            int x = left + 125 + i * 88;
            boolean selected = ClientConfig.hudColor() == colors[i];
            context.fill(x, top + 251, x + 78, top + 278, selected ? 0xFF3A2748 : 0xFF24202B);
            context.fill(x + 7, top + 258, x + 20, top + 271, colors[i]);
            context.drawText(textRenderer, Text.literal(names[i]), x + 26, top + 258, selected ? 0xFFE8D7F1 : 0xFF8B8290, false);
        }

        context.drawText(textRenderer, Text.literal("PLAN"), left + 22, top + 305, 0xFFD8CFDD, false);
        drawPlan(context, left + 125, top + 296, "FREE", !ClientConfig.isPlus());
        drawPlan(context, left + 240, top + 296, "PLUS", ClientConfig.isPlus());
        context.drawText(textRenderer, Text.literal(ClientConfig.isPlus() ? "Plus features unlocked" : "Plus features require a subscription"), left + 365, top + 304, 0xFF8F8794, false);

        context.fill(left + 22, bottom - 52, left + 135, bottom - 27, 0xFF2A202F);
        context.drawText(textRenderer, Text.literal("Reset"), left + 55, bottom - 45, 0xFFE2D8E6, false);
        context.fill(left + 155, bottom - 52, left + 300, bottom - 27, 0xFF2A2232);
        context.drawText(textRenderer, Text.literal("HUD Editor"), left + 188, bottom - 45, 0xFFE8DFF0, false);
        context.drawText(textRenderer, Text.literal("ESC  Back"), right - 88, bottom - 45, 0xFF8F8794, false);

        if (resetConfirm) {
            context.fill(left + 130, top + 125, right - 130, top + 235, 0xFF211B27);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Reset all settings?"), width / 2, top + 145, 0xFFF0E9F2);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Click Reset again to confirm"), width / 2, top + 168, 0xFF8D8291);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext c, int x, int y, String label, boolean enabled, int mx, int my) {
        c.drawText(textRenderer, Text.literal(label), x, y + 4, 0xFFD8CFDD, false);
        int bx = x + 455;
        boolean hovered = inside(mx, my, bx, y, 50, 24);
        c.fill(bx, y, bx + 50, y + 24, enabled ? (ClientConfig.hudColor() & 0x00FFFFFF) | 0xFF300F3F : hovered ? 0xFF2E2833 : 0xFF24202B);
        c.drawText(textRenderer, Text.literal(enabled ? "ON" : "OFF"), bx + 15, y + 5, enabled ? 0xFFE9D6F4 : 0xFF827984, false);
    }

    private void drawSlider(DrawContext c, int x, int y, String label, int value, int min, int max, String format, int mx, int my) {
        c.drawText(textRenderer, Text.literal(label), x, y + 4, 0xFFD8CFDD, false);
        c.drawText(textRenderer, Text.literal(String.format(format, value)), x + 455, y + 4, 0xFFAAA0AE, false);
        int barLeft = x + 195, barRight = x + 430, barY = y + 10;
        c.fill(barLeft, barY, barRight, barY + 4, 0xFF2A2530);
        int fill = barLeft + (int)((barRight - barLeft) * ((value - min) / (double)(max - min)));
        c.fill(barLeft, barY, fill, barY + 4, ClientConfig.hudColor());
        c.fill(fill - 4, barY - 4, fill + 4, barY + 8, 0xFFE6C8F0);
    }

    private void drawPlan(DrawContext c, int x, int y, String name, boolean selected) {
        c.fill(x, y, x + 100, y + 28, selected ? 0xFF3A2748 : 0xFF24202B);
        c.drawText(textRenderer, Text.literal(name), x + 33, y + 7, selected ? ClientConfig.hudColor() : 0xFF8B8290, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y(); int left = left(), top = top(), bottom = top + HEIGHT;
        if (inside(mx, my, left + 477, top + 68, 50, 24)) { ClientConfig.setAnimations(!ClientConfig.animations()); return true; }
        if (inside(mx, my, left + 477, top + 108, 50, 24)) { ClientConfig.setHoverEffects(!ClientConfig.hoverEffects()); return true; }
        if (inside(mx, my, left + 217, top + 147, 235, 24)) { ClientConfig.setGuiScale(sliderValue(mx, left + 217, left + 452, 80, 125)); return true; }
        if (inside(mx, my, left + 217, top + 197, 235, 24)) { ClientConfig.setOverlayOpacity(sliderValue(mx, left + 217, left + 452, 20, 85)); return true; }
        int[] colors = {0xFFB15CFF, 0xFF8D67FF, 0xFFE26BFF, 0xFF36D7FF, 0xFFFF5C91};
        for (int i = 0; i < colors.length; i++) { int x = left + 125 + i * 88; if (inside(mx, my, x, top + 251, 78, 27)) { ClientConfig.setHudColor(colors[i]); return true; } }
        if (inside(mx, my, left + 240, top + 296, 100, 28)) { ClientConfig.setPlan("PLUS"); return true; }
        if (inside(mx, my, left + 125, top + 296, 100, 28)) { ClientConfig.setPlan("FREE"); return true; }
        if (inside(mx, my, left + 155, bottom - 52, 145, 25)) { client.setScreen(new HudEditorScreen()); return true; }
        if (inside(mx, my, left + 22, bottom - 52, 113, 25)) { if (resetConfirm) { ClientConfig.reset(); resetConfirm = false; } else resetConfirm = true; return true; }
        return super.mouseClicked(click, doubled);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
    private static int sliderValue(double mx, int left, int right, int min, int max) { double t = Math.max(0, Math.min(1, (mx-left)/(double)(right-left))); return min + (int)Math.round((max-min)*t); }
    @Override public boolean keyPressed(KeyInput input) { if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; } return super.keyPressed(input); }
    @Override public void close() { if (client != null) client.setScreen(null); }
    @Override public boolean shouldPause() { return false; }
}
