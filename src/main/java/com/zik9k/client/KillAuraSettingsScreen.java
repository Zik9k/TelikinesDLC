package com.zik9k.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class KillAuraSettingsScreen extends Screen {
    private static final int WIDTH = 520;
    private static final int HEIGHT = 330;
    private final KillAuraModule module;
    private final Screen parent;
    private boolean draggingRange;
    private boolean draggingCps;

    public KillAuraSettingsScreen(KillAuraModule module, Screen parent) {
        super(Text.literal("Kill Aura Settings"));
        this.module = module;
        this.parent = parent;
    }

    private int left() { return (width - WIDTH) / 2; }
    private int top() { return (height - HEIGHT) / 2; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        int l = left(), t = top(), r = l + WIDTH, b = t + HEIGHT;
        c.fill(0, 0, width, height, 0x8A08060D);
        c.fill(l + 5, t + 7, r + 5, b + 7, 0x42000000);
        c.fill(l + 2, t, r - 2, b, 0xFF17131F);
        c.fill(l, t + 2, r, b - 2, 0xFF17131F);
        c.fill(l + 2, t + 2, r - 2, t + 48, 0xFF1A1622);

        c.drawText(textRenderer, Text.literal("Kill Aura"), l + 20, t + 17, 0xFFF4EEF9, false);
        c.drawText(textRenderer, Text.literal("Settings"), l + 104, t + 18, 0xFF776E7E, false);

        c.drawText(textRenderer, Text.literal("Targets"), l + 22, t + 68, 0xFFBDB4C4, false);
        drawToggle(c, l + 22, t + 86, "Players", module.players(), mx, my);
        drawToggle(c, l + 272, t + 86, "Mobs", module.mobs(), mx, my);
        drawToggle(c, l + 22, t + 130, "Animals", module.animals(), mx, my);

        drawSlider(c, l + 22, t + 178, "Range", module.range(), 3, 6, "%d blocks");
        drawSlider(c, l + 22, t + 232, "Attack rate", module.cps(), 1, 20, "%d CPS");

        c.drawText(textRenderer, Text.literal("Uses vanilla attack cooldown"), l + 22, b - 38, 0xFF655D69, false);
        c.drawText(textRenderer, Text.literal("ESC"), r - 62, b - 38, 0xFF8F8794, false);
        c.drawText(textRenderer, Text.literal("Back"), r - 36, b - 38, 0xFF6F6673, false);
        super.render(c, mx, my, delta);
    }

    private void drawToggle(DrawContext c, int x, int y, String label, boolean enabled, int mx, int my) {
        c.drawText(textRenderer, Text.literal(label), x, y + 5, 0xFFD8CFDD, false);
        int bl = x + 145, br = bl + 58;
        boolean hovered = mx >= bl && mx <= br && my >= y && my <= y + 24;
        c.fill(bl, y, br, y + 24, enabled ? 0xFF4D315F : hovered ? 0xFF2E2833 : 0xFF24202B);
        c.drawText(textRenderer, Text.literal(enabled ? "ON" : "OFF"), bl + 18, y + 5, enabled ? 0xFFE9D6F4 : 0xFF827984, false);
    }

    private void drawSlider(DrawContext c, int x, int y, String label, int value, int min, int max, String format) {
        c.drawText(textRenderer, Text.literal(label), x, y + 4, 0xFFD8CFDD, false);
        c.drawText(textRenderer, Text.literal(String.format(format, value)), x + 410, y + 4, 0xFFAAA0AE, false);
        int bl = x + 175, br = x + 390, by = y + 10;
        c.fill(bl, by, br, by + 4, 0xFF2A2530);
        int fill = bl + (int) ((br - bl) * ((value - min) / (double) (max - min)));
        c.fill(bl, by, fill, by + 4, 0xFFB15CFF);
        c.fill(fill - 4, by - 4, fill + 4, by + 8, 0xFFC381E9);
    }

    private void updateRange(double mouseX) { module.setRange(sliderValue(mouseX, left() + 197, left() + 412, 3, 6)); }
    private void updateCps(double mouseX) { module.setCps(sliderValue(mouseX, left() + 197, left() + 412, 1, 20)); }
    private static int sliderValue(double mouseX, int l, int r, int min, int max) {
        double t = Math.max(0.0, Math.min(1.0, (mouseX - l) / (double) (r - l)));
        return min + (int) Math.round((max - min) * t);
    }
    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int l = left(), t = top();
        if (inside(mx, my, l + 167, t + 86, 58, 24)) { module.setPlayers(!module.players()); return true; }
        if (inside(mx, my, l + 417, t + 86, 58, 24)) { module.setMobs(!module.mobs()); return true; }
        if (inside(mx, my, l + 167, t + 130, 58, 24)) { module.setAnimals(!module.animals()); return true; }
        if (mx >= l + 197 && mx <= l + 412 && my >= t + 164 && my <= t + 198) { draggingRange = true; updateRange(mx); return true; }
        if (mx >= l + 197 && mx <= l + 412 && my >= t + 218 && my <= t + 252) { draggingCps = true; updateCps(mx); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (click.button() == 0 && draggingRange) { updateRange(click.x()); return true; }
        if (click.button() == 0 && draggingCps) { updateCps(click.x()); return true; }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) { draggingRange = false; draggingCps = false; }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override public void close() { if (client != null) client.setScreen(parent); }
    @Override public boolean shouldPause() { return false; }
}
