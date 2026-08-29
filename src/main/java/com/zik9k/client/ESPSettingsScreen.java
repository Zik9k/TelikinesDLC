package com.zik9k.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ESPSettingsScreen extends Screen {
    private static final int WIDTH = 520;
    private static final int HEIGHT = 320;
    private final ESPModule module;
    private final Screen parent;
    private boolean draggingRange;

    public ESPSettingsScreen(ESPModule module, Screen parent) {
        super(Text.literal("ESP Settings"));
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

        context.drawText(textRenderer, Text.literal("ESP"), left + 20, top + 17, 0xFFF4EEF9, false);
        context.drawText(textRenderer, Text.literal("Render settings"), left + 65, top + 18, 0xFF776E7E, false);

        context.drawText(textRenderer, Text.literal("Targets"), left + 22, top + 68, 0xFFBDB4C4, false);
        drawToggle(context, left + 22, top + 86, "Players", module.players(), mouseX, mouseY);
        drawToggle(context, left + 272, top + 86, "Mobs", module.mobs(), mouseX, mouseY);
        drawToggle(context, left + 22, top + 130, "Animals", module.animals(), mouseX, mouseY);

        context.drawText(textRenderer, Text.literal("Render distance"), left + 22, top + 180, 0xFFBDB4C4, false);
        context.drawText(textRenderer, Text.literal(module.range() + " blocks"), right - 105, top + 180, 0xFFAFA5B5, false);
        int barLeft = left + 22, barRight = right - 22, barY = top + 202;
        context.fill(barLeft, barY, barRight, barY + 4, 0xFF2A2530);
        int fill = barLeft + (int) ((barRight - barLeft) * ((module.range() - 8) / 120.0));
        context.fill(barLeft, barY, fill, barY + 4, 0xFFB15CFF);
        context.fill(fill - 4, barY - 4, fill + 4, barY + 8, 0xFFC381E9);

        context.drawText(textRenderer, Text.literal("ESP uses Minecraft's entity outline renderer."), left + 22, bottom - 48, 0xFF655D69, false);
        context.drawText(textRenderer, Text.literal("ESC"), right - 62, bottom - 38, 0xFF8F8794, false);
        context.drawText(textRenderer, Text.literal("Back"), right - 36, bottom - 38, 0xFF6F6673, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext context, int x, int y, String label, boolean enabled, int mouseX, int mouseY) {
        context.drawText(textRenderer, Text.literal(label), x, y + 5, 0xFFD8CFDD, false);
        int boxLeft = x + 145, boxRight = boxLeft + 58;
        boolean hovered = mouseX >= boxLeft && mouseX <= boxRight && mouseY >= y && mouseY <= y + 24;
        context.fill(boxLeft, y, boxRight, y + 24, enabled ? 0xFF4D315F : hovered ? 0xFF2E2833 : 0xFF24202B);
        context.drawText(textRenderer, Text.literal(enabled ? "ON" : "OFF"), boxLeft + 18, y + 5, enabled ? 0xFFE9D6F4 : 0xFF827984, false);
    }

    private void updateRange(double mouseX) {
        int left = left();
        int start = left + 22;
        int end = left + WIDTH - 22;
        double t = Math.max(0.0, Math.min(1.0, (mouseX - start) / (double) (end - start)));
        module.setRange(8 + (int) Math.round(120.0 * t));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        int button = click.button();
        if (button != 0) return super.mouseClicked(click, doubled);
        int left = left(), top = top();
        if (inside(mouseX, mouseY, left + 167, top + 86, 58, 24)) { module.setPlayers(!module.players()); return true; }
        if (inside(mouseX, mouseY, left + 417, top + 86, 58, 24)) { module.setMobs(!module.mobs()); return true; }
        if (inside(mouseX, mouseY, left + 167, top + 130, 58, 24)) { module.setAnimals(!module.animals()); return true; }
        if (mouseY >= top + 190 && mouseY <= top + 220) { draggingRange = true; updateRange(mouseX); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingRange && click.button() == 0) { updateRange(click.x()); return true; }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) draggingRange = false;
        return super.mouseReleased(click);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
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
