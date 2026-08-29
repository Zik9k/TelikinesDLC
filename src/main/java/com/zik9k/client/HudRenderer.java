package com.zik9k.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/** TelikinesDLC HUD with active modules, keybinds, performance and player info. */
@Environment(EnvType.CLIENT)
public final class HudRenderer {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private HudRenderer() { }
    public static void register() { HudRenderCallback.EVENT.register(HudRenderer::render); }
    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        int accent = switch (ClientConfig.accent()) { case 1 -> 0xFF8D67FF; case 2 -> 0xFFE26BFF; default -> 0xFFB15CFF; };
        drawActiveModules(context, client, accent);
        drawKeybinds(context, client, accent);
        drawInfoPanel(context, client, accent);
        drawStaffList(context, client, accent);
    }
    private static void drawPanel(DrawContext context, int x, int y, int width, int height, int accent) {
        context.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x38000000);
        context.fill(x, y, x + width, y + height, 0x66090710);
        context.fill(x, y, x + 2, y + height, accent);
    }
    private static void drawTitle(DrawContext context, MinecraftClient client, String title, int x, int y, int accent) {
        context.drawText(client.textRenderer, Text.literal(title), x + 8, y + 6, 0xFFF1EAF5, true);
    }
    private static void drawTlMark(DrawContext context, int x, int y, int accent) {
        context.fill(x, y, x + 28, y + 28, 0x7A100B18);
        context.fill(x, y, x + 2, y + 28, accent);
        context.fill(x + 6, y + 4, x + 22, y + 7, accent);
        context.fill(x + 12, y + 6, x + 16, y + 22, accent);
        context.fill(x + 8, y + 10, x + 12, y + 13, 0xFFF1EAF5);
        context.fill(x + 8, y + 13, x + 20, y + 16, 0xFFF1EAF5);
        context.fill(x + 8, y + 16, x + 11, y + 22, 0xFFF1EAF5);
    }
    private static void drawActiveModules(DrawContext context, MinecraftClient client, int accent) {
        List<Module> active = ModuleManager.getModules().stream().filter(Module::isEnabled).sorted(Comparator.comparingInt(m -> -client.textRenderer.getWidth(m.getName()))).toList();
        if (active.isEmpty()) return;
        int right = client.getWindow().getScaledWidth() - 8, x = right - 164, y = 8, rowHeight = 15;
        drawPanel(context, x, y, 164, 27 + active.size() * rowHeight, accent);
        drawTitle(context, client, "Active Modules", x, y, accent);
        int rowY = y + 22;
        for (Module module : active) {
            int textWidth = client.textRenderer.getWidth(module.getName());
            context.drawText(client.textRenderer, Text.literal(module.getName()), right - textWidth - 12, rowY, 0xFFF1EAF5, false);
            rowY += rowHeight;
        }
    }
    private static void drawKeybinds(DrawContext context, MinecraftClient client, int accent) {
        int x = 8, y = 8, width = 148, height = 47;
        drawPanel(context, x, y, width, height, accent);
        drawTitle(context, client, "Keybinds", x, y, accent);
        context.drawText(client.textRenderer, Text.literal("ClickGUI"), x + 8, y + 24, 0xFFB7ADBA, false);
        context.drawText(client.textRenderer, Text.literal("RSHIFT"), x + width - 49, y + 24, accent, false);
    }
    private static void drawInfoPanel(DrawContext context, MinecraftClient client, int accent) {
        int x = 8, y = client.getWindow().getScaledHeight() - 75, width = 230, height = 67;
        drawPanel(context, x, y, width, height, accent);
        drawTlMark(context, x + 7, y + 5, accent);
        context.drawText(client.textRenderer, Text.literal("TELIKINESDLC"), x + 42, y + 6, 0xFFF1EAF5, true);
        context.drawText(client.textRenderer, Text.literal("1.21.11"), x + 42, y + 20, accent, false);
        int fps = client.getCurrentFps(), ping = getPing(client);
        String coords = String.format("XYZ %d %d %d", client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
        String clock = LocalTime.now().format(CLOCK);
        context.drawText(client.textRenderer, Text.literal("FPS  " + fps), x + 8, y + 42, 0xFFD6CCD9, false);
        context.drawText(client.textRenderer, Text.literal("Ping " + (ping >= 0 ? ping + " ms" : "--")), x + 82, y + 42, 0xFFD6CCD9, false);
        context.drawText(client.textRenderer, Text.literal(coords), x + 8, y + 56, 0xFFAFA5B3, false);
        context.drawText(client.textRenderer, Text.literal(clock), x + width - 58, y + 56, accent, false);
    }
    private static int getPing(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) return -1;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry == null ? -1 : entry.getLatency();
    }
    private static void drawStaffList(DrawContext context, MinecraftClient client, int accent) {
        if (client.getNetworkHandler() == null) return;
        List<PlayerListEntry> staff = client.getNetworkHandler().getPlayerList().stream()
                .filter(entry -> entry.getGameMode() != null && !entry.getGameMode().isSurvivalLike())
                .sorted(Comparator.comparing(entry -> entry.getProfile().name()))
                .toList();
        if (staff.isEmpty()) return;
        int x = 8, y = 62, width = 190, height = 30 + Math.min(staff.size(), 6) * 15;
        drawPanel(context, x, y, width, height, accent);
        drawTitle(context, client, "Staff List", x, y, accent);
        int rowY = y + 22;
        for (PlayerListEntry entry : staff) {
            if (rowY > y + height - 10) break;
            String name = entry.getProfile().name();
            context.drawText(client.textRenderer, Text.literal(name), x + 8, rowY, 0xFFD8CFDD, false);
            context.drawText(client.textRenderer, Text.literal("listed"), x + width - 42, rowY, accent, false);
            rowY += 15;
        }
    }
}
