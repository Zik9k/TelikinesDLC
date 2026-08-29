package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

/** Minimal TelikinesDLC HUD inspired by the supplied reference, without copying it. */
public final class HudRenderer {
    private HudRenderer() { }

    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::render);
    }

    private static void render(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        int accent = switch (ClientConfig.accent()) {
            case 1 -> 0xFF8D67FF;
            case 2 -> 0xFFE26BFF;
            default -> 0xFFB15CFF;
        };

        drawActiveModules(context, client, accent);
        drawKeybinds(context, client, accent);
    }

    private static void drawActiveModules(DrawContext context, MinecraftClient client, int accent) {
        List<Module> active = ModuleManager.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -client.textRenderer.getWidth(m.getName())))
                .toList();
        if (active.isEmpty()) return;

        int right = client.getWindow().getScaledWidth() - 8;
        int y = 8;
        int rowHeight = 16;
        int maxWidth = active.stream().mapToInt(m -> client.textRenderer.getWidth(m.getName())).max().orElse(0);
        int panelLeft = right - maxWidth - 22;
        int panelRight = right + 1;
        int panelTop = y - 4;
        int panelBottom = y + active.size() * rowHeight + 4;

        context.fill(panelLeft, panelTop, panelRight, panelBottom, 0x5A09070E);
        context.fill(panelRight - 2, panelTop, panelRight, panelBottom, accent);
        context.drawText(client.textRenderer, Text.literal("TELIKINESDLC"), panelLeft + 8, y - 1, accent, true);
        y += 14;

        for (Module module : active) {
            String name = module.getName();
            int textWidth = client.textRenderer.getWidth(name);
            int textX = right - textWidth;
            context.drawText(client.textRenderer, Text.literal(name), textX, y, 0xFFF1EAF5, true);
            y += rowHeight;
        }
    }

    private static void drawKeybinds(DrawContext context, MinecraftClient client, int accent) {
        int x = 8;
        int y = 8;
        int width = 138;
        int height = 44;
        context.fill(x, y, x + width, y + height, 0x5A09070E);
        context.fill(x, y, x + 2, y + height, accent);
        context.drawText(client.textRenderer, Text.literal("Keybinds"), x + 8, y + 7, 0xFFF1EAF5, true);
        context.drawText(client.textRenderer, Text.literal("ClickGUI"), x + 8, y + 23, 0xFFB7ADBA, false);
        context.drawText(client.textRenderer, Text.literal("RSHIFT"), x + width - 47, y + 23, accent, false);
    }
}
