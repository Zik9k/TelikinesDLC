package com.zik9k.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class TriggerBotSettingsScreen extends Screen {
    private static final int WIDTH = 520;
    private static final int HEIGHT = 330;
    private final TriggerBotModule module;
    private final Screen parent;
    private boolean draggingCps;

    public TriggerBotSettingsScreen(TriggerBotModule module, Screen parent) {
        super(Text.literal("Trigger Bot Settings"));
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

        context.drawText(textRenderer, Text.literal("Trigger Bot"), left + 20, top + 17, 0xFFF4EEF9, false);
        context.drawText(textRenderer, Text.literal("Settings"), left + 112, top + 18, 0xFF776E7E, false);

        context.drawText(textRenderer, Text.literal("Attack modes"), left + 22, top + 68, 0xFFBDB4C4, false);
        drawToggle(context, left + 22, top + 86, "Clicks", module.isClickMode(), mouseX, mouseY);
        drawToggle(context, left + 272, top + 86, "Crits", module.isCritMode(), mouseX, mouseY);

        context.drawText(textRenderer, Text.literal("Click speed"), left + 22, top + 132, 0xFFBDB4C4, false);
        context.drawText(textRenderer, Text.literal(module.getClicksPerSecond() + " CPS"), right - 78, top + 132, 0xFFAFA5B5, false);
        int barLeft = left + 22, barRight = right - 22, barY = top + 154;
        context.fill(barLeft, barY, barRight, barY + 4, 0xFF2A2530);
        int fill = barLeft + (int) ((barRight - barLeft) * ((module.getClicksPerSecond() - 1) / 19.0));
        context.fill(barLeft, barY, fill, barY + 4, 0xFFB15CFF);
        context.fill(fill - 4, barY - 4, fill + 4, barY + 8, 0xFFC381E9);

        context.drawText(textRenderer, Text.literal("Targets"), left + 22, top + 184, 0xFFBDB4C4, false);
        drawToggle(context, left + 22, top + 202, "Mobs", module.isTargetMobs(), mouseX, mouseY);
        drawToggle(context, left + 272, top + 202, "Animals", module.isTargetAnimals(), mouseX, mouseY);
        drawToggle(context, left + 22, top + 246, "Players", module.isTargetPlayers(), mouseX, mouseY);

        context.drawText(textRenderer, Text.literal("RMB from ClickGUI opens these settings"), left + 22, bottom - 38, 0xFF655D69, false);
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

    private boolean inSlider(double mouseX, double mouseY) {
        int left = left();
        int top = top();
        return mouseX >= left + 22 && mouseX <= left + WIDTH - 22 && mouseY >= top + 140 && mouseY <= top + 170;
    }

    private void updateCps(double mouseX) {
        int left = left();
        int right = left + WIDTH;
        module.setClicksPerSecond(sliderValue(mouseX, left + 22, right - 22, 1, 20));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int left = left(), top = top(), right = left + WIDTH;

        if (inside(mouseX, mouseY, left + 167, top + 86, 58, 24)) {
            module.setClickMode(!module.isClickMode());
            return true;
        }
        if (inside(mouseX, mouseY, left + 417, top + 86, 58, 24)) {
            module.setCritMode(!module.isCritMode());
            return true;
        }
        if (inSlider(mouseX, mouseY)) {
            draggingCps = true;
            updateCps(mouseX);
            return true;
        }
        if (inside(mouseX, mouseY, left + 167, top + 202, 58, 24)) {
            module.setTargetMobs(!module.isTargetMobs());
            return true;
        }
        if (inside(mouseX, mouseY, left + 417, top + 202, 58, 24)) {
            module.setTargetAnimals(!module.isTargetAnimals());
            return true;
        }
        if (inside(mouseX, mouseY, left + 167, top + 246, 58, 24)) {
            module.setTargetPlayers(!module.isTargetPlayers());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingCps) {
            updateCps(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingCps = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static int sliderValue(double mouseX, int left, int right, int min, int max) {
        double t = Math.max(0.0, Math.min(1.0, (mouseX - left) / (double) (right - left)));
        return min + (int) Math.round((max - min) * t);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
