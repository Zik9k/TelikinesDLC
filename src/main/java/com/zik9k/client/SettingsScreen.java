package com.zik9k.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class SettingsScreen extends Screen {
    private static final int WIDTH = 560;
    private static final int HEIGHT = 360;
    private boolean resetConfirm;

    public SettingsScreen() {
        super(Text.literal("TelikinesDLC Settings"));
    }

    @Override
    protected void init() {
    }

    private int left() { return (width - WIDTH) / 2; }
    private int top() { return (height - HEIGHT) / 2; }

    private void toggleSetting(int id) {
        switch (id) {
            case 0 -> ClientConfig.setAnimations(!ClientConfig.animations());
            case 1 -> ClientConfig.setHoverEffects(!ClientConfig.hoverEffects());
            default -> { }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x8A08060D);

        int left = left();
        int top = top();
        int right = left + WIDTH;
        int bottom = top + HEIGHT;

        context.fill(left + 5, top + 7, right + 5, bottom + 7, 0x42000000);
        context.fill(left + 2, top, right - 2, bottom, 0xFF17131F);
        context.fill(left, top + 2, right, bottom - 2, 0xFF17131F);
        context.fill(left + 2, top + 2, right - 2, top + 48, 0xFF1A1622);

        context.drawText(textRenderer, Text.literal("Settings"), left + 20, top + 17, 0xFFF4EEF9, false);
        context.drawText(textRenderer, Text.literal("TelikinesDLC client configuration"), left + 98, top + 18, 0xFF776E7E, false);

        drawToggle(context, left + 22, top + 72, "Animations", ClientConfig.animations(), mouseX, mouseY);
        drawToggle(context, left + 22, top + 116, "Hover effects", ClientConfig.hoverEffects(), mouseX, mouseY);

        drawSlider(context, left + 22, top + 174, "GUI scale", ClientConfig.guiScale(), 80, 125, "%d%%");
        drawSlider(context, left + 22, top + 228, "Overlay opacity", ClientConfig.overlayOpacity(), 20, 85, "%d%%");

        context.drawText(textRenderer, Text.literal("Accent"), left + 22, top + 278, 0xFFD8CFDD, false);
        String[] accents = {"Purple", "Violet", "Pink"};
        for (int i = 0; i < accents.length; i++) {
            int x = left + 100 + i * 82;
            boolean selected = ClientConfig.accent() == i;
            context.fill(x, top + 272, x + 72, top + 294, selected ? 0xFF3A2748 : 0xFF24202B);
            context.drawText(textRenderer, Text.literal(accents[i]), x + 9, top + 278, selected ? 0xFFE8D7F1 : 0xFF8B8290, false);
        }

        context.fill(left + 22, bottom - 54, left + 124, bottom - 30, 0xFF2A202F);
        context.drawText(textRenderer, Text.literal("Reset"), left + 51, bottom - 48, 0xFFE2D8E6, false);
        context.drawText(textRenderer, Text.literal("ESC"), right - 67, bottom - 48, 0xFF6F6673, false);
        context.drawText(textRenderer, Text.literal("Back"), right - 37, bottom - 48, 0xFF8F8794, false);

        if (resetConfirm) {
            context.fill(left + 110, top + 122, right - 110, top + 232, 0xFF211B27);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Reset all settings?"), width / 2, top + 142, 0xFFF0E9F2);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Click Reset again to confirm"), width / 2, top + 164, 0xFF8D8291);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext context, int x, int y, String label, boolean enabled, int mouseX, int mouseY) {
        context.drawText(textRenderer, Text.literal(label), x, y + 4, 0xFFD8CFDD, false);
        int boxLeft = x + 405;
        int boxRight = x + 455;
        boolean hovered = mouseX >= boxLeft && mouseX <= boxRight && mouseY >= y && mouseY <= y + 24;
        context.fill(boxLeft, y, boxRight, y + 24, enabled ? 0xFF4D315F : hovered ? 0xFF2E2833 : 0xFF24202B);
        context.drawText(textRenderer, Text.literal(enabled ? "ON" : "OFF"), boxLeft + 16, y + 5, enabled ? 0xFFE9D6F4 : 0xFF827984, false);
    }

    private void drawSlider(DrawContext context, int x, int y, String label, int value, int min, int max, String format) {
        context.drawText(textRenderer, Text.literal(label), x, y + 4, 0xFFD8CFDD, false);
        context.drawText(textRenderer, Text.literal(String.format(format, value)), x + 410, y + 4, 0xFFAAA0AE, false);
        int barLeft = x + 175;
        int barRight = x + 390;
        int barY = y + 10;
        context.fill(barLeft, barY, barRight, barY + 4, 0xFF2A2530);
        int fill = barLeft + (int) ((barRight - barLeft) * ((value - min) / (double) (max - min)));
        context.fill(barLeft, barY, fill, barY + 4, 0xFFA85DDE);
        context.fill(fill - 4, barY - 4, fill + 4, barY + 8, 0xFFC381E9);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button != 0) return super.mouseClicked(click, doubled);
        int left = left();
        int top = top();
        int bottom = top + HEIGHT;

        if (inside(mouseX, mouseY, left + 427, top + 72, 50, 24)) { toggleSetting(0); return true; }
        if (inside(mouseX, mouseY, left + 427, top + 116, 50, 24)) { toggleSetting(1); return true; }
        if (inside(mouseX, mouseY, left + 197, top + 164, 215, 24)) {
            ClientConfig.setGuiScale(sliderValue(mouseX, left + 197, left + 412, 80, 125));
            return true;
        }
        if (inside(mouseX, mouseY, left + 197, top + 218, 215, 24)) {
            ClientConfig.setOverlayOpacity(sliderValue(mouseX, left + 197, left + 412, 20, 85));
            return true;
        }

        String[] accents = {"Purple", "Violet", "Pink"};
        for (int i = 0; i < accents.length; i++) {
            int x = left + 100 + i * 82;
            if (inside(mouseX, mouseY, x, top + 272, 72, 22)) {
                ClientConfig.setAccent(i);
                return true;
            }
        }

        if (inside(mouseX, mouseY, left + 22, bottom - 54, 102, 24)) {
            if (resetConfirm) {
                ClientConfig.reset();
                resetConfirm = false;
            } else {
                resetConfirm = true;
            }
            return true;
        }
        if (inside(mouseX, mouseY, left + 280, bottom - 62, 240, 40)) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static int sliderValue(double mouseX, int left, int right, int min, int max) {
        double t = Math.max(0.0, Math.min(1.0, (mouseX - left) / (double) (right - left)));
        return min + (int) Math.round((max - min) * t);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }
}
