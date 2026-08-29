package com.zik9k.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class BlockOverlaySettingsScreen extends Screen {
    private static final int WIDTH = 520;
    private static final int HEIGHT = 280;
    private final BlockOverlayModule module;
    private final Screen parent;
    private boolean draggingFill;
    private boolean draggingOutline;

    public BlockOverlaySettingsScreen(BlockOverlayModule module, Screen parent) {
        super(Text.literal("Block Overlay Settings"));
        this.module = module;
        this.parent = parent;
    }

    private int left() { return (width - WIDTH) / 2; }
    private int top() { return (height - HEIGHT) / 2; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int left = left(), top = top(), right = left + WIDTH, bottom = top + HEIGHT;
        context.fill(0, 0, width, height, 0x8A08060D);
        context.fill(left + 5, top + 7, right + 5, bottom + 7, 0x42000000);
        context.fill(left + 2, top, right - 2, bottom, 0xFF17131F);
        context.fill(left, top + 2, right, bottom - 2, 0xFF17131F);
        context.fill(left + 2, top + 2, right - 2, top + 48, 0xFF1A1622);

        context.drawText(textRenderer, Text.literal("Block Overlay"), left + 20, top + 17, 0xFFF4EEF9, false);
        context.drawText(textRenderer, Text.literal("Render settings"), left + 120, top + 18, 0xFF776E7E, false);

        drawSlider(context, left + 22, top + 76, "Fill opacity", module.alpha(), 10, 180, mouseX, mouseY);
        drawSlider(context, left + 22, top + 144, "Outline opacity", module.outlineAlpha(), 40, 255, mouseX, mouseY);
        context.drawText(textRenderer, Text.literal("Only the block under your crosshair is highlighted."), left + 22, top + 212, 0xFF7F7583, false);
        context.drawText(textRenderer, Text.literal("ESC"), right - 62, bottom - 38, 0xFF8F8794, false);
        context.drawText(textRenderer, Text.literal("Back"), right - 36, bottom - 38, 0xFF6F6673, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSlider(DrawContext context, int x, int y, String label, int value, int min, int max, int mouseX, int mouseY) {
        context.drawText(textRenderer, Text.literal(label), x, y + 4, 0xFFD8CFDD, false);
        context.drawText(textRenderer, Text.literal(Integer.toString(value)), x + 410, y + 4, 0xFFAAA0AE, false);
        int barLeft = x + 175, barRight = x + 390, barY = y + 10;
        context.fill(barLeft, barY, barRight, barY + 4, 0xFF2A2530);
        int fill = barLeft + (int) ((barRight - barLeft) * ((value - min) / (double) (max - min)));
        context.fill(barLeft, barY, fill, barY + 4, 0xFFB15CFF);
        boolean hovered = mouseX >= barLeft && mouseX <= barRight && mouseY >= barY - 7 && mouseY <= barY + 11;
        context.fill(fill - 4, barY - 4, fill + 4, barY + 8, hovered ? 0xFFD28AF1 : 0xFFC381E9);
    }

    private boolean inSlider(double mouseX, double mouseY, int y) {
        int left = left();
        return mouseX >= left + 197 && mouseX <= left + 412 && mouseY >= y - 7 && mouseY <= y + 11;
    }

    private int sliderValue(double mouseX, int min, int max) {
        int left = left() + 197, right = left() + 412;
        double t = Math.max(0.0, Math.min(1.0, (mouseX - left) / (double) (right - left)));
        return min + (int) Math.round((max - min) * t);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int top = top();
        if (inSlider(mouseX, mouseY, top + 86)) { draggingFill = true; module.setAlpha(sliderValue(mouseX, 10, 180)); return true; }
        if (inSlider(mouseX, mouseY, top + 154)) { draggingOutline = true; module.setOutlineAlpha(sliderValue(mouseX, 40, 255)); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (click.button() == 0) {
            if (draggingFill) { module.setAlpha(sliderValue(click.x(), 10, 180)); return true; }
            if (draggingOutline) { module.setOutlineAlpha(sliderValue(click.x(), 40, 255)); return true; }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) { draggingFill = false; draggingOutline = false; }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
