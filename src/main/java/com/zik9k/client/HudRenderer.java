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

@Environment(EnvType.CLIENT)
public final class HudRenderer {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private HudRenderer() { }

    public static void register() { HudRenderCallback.EVENT.register(HudRenderer::render); }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        int accent = ClientConfig.hudColor();
        drawActiveModules(context, client, accent);
        drawKeybinds(context, client, accent);
        drawInfoPanel(context, client, accent);
        drawStaffList(context, client, accent);
    }

    private static void drawPanel(DrawContext context, int x, int y, int width, int height, int accent) {
        context.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x42000000);
        context.fill(x, y, x + width, y + height, 0x9A09070F);
        context.fill(x, y, x + width, y + 2, accent);
        context.fill(x, y + 2, x + 2, y + height, accent);
    }

    private static void drawTitle(DrawContext context, MinecraftClient client, String title, int x, int y) {
        context.drawText(client.textRenderer, Text.literal(title), x + 9, y + 6, 0xFFF5EFF7, true);
    }

    private static void drawActiveModules(DrawContext context, MinecraftClient client, int accent) {
        List<Module> active = ModuleManager.getModules().stream().filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -client.textRenderer.getWidth(m.getName()))).toList();
        if (active.isEmpty()) return;
        int x = ClientConfig.hudActiveX(), y = ClientConfig.hudActiveY();
        int width = 180, rowHeight = 16;
        drawPanel(context, x, y, width, 27 + active.size() * rowHeight, accent);
        drawTitle(context, client, "ACTIVE MODULES", x, y);
        int rowY = y + 22;
        for (Module module : active) {
            context.drawText(client.textRenderer, Text.literal(module.getName()), x + 9, rowY, 0xFFEDE6F1, false);
            context.fill(x + width - 49, rowY + 1, x + width - 9, rowY + 11, (accent & 0x00FFFFFF) | 0x33000000);
            rowY += rowHeight;
        }
    }

    private static void drawKeybinds(DrawContext context, MinecraftClient client, int accent) {
        int x = ClientConfig.hudKeybindX(), y = ClientConfig.hudKeybindY();
        int width = 164, height = 50;
        drawPanel(context, x, y, width, height, accent);
        drawTitle(context, client, "KEYBINDS", x, y);
        context.drawText(client.textRenderer, Text.literal("ClickGUI"), x + 9, y + 25, 0xFFBFB5C4, false);
        context.fill(x + 108, y + 22, x + 154, y + 37, 0xFF241F2A);
        context.drawText(client.textRenderer, Text.literal("RSHIFT"), x + 116, y + 26, accent, false);
    }

    private static void drawInfoPanel(DrawContext context, MinecraftClient client, int accent) {
        int width = 248, height = 70;
        int x = ClientConfig.hudInfoX();
        int y = ClientConfig.hudInfoY() <= 0 ? client.getWindow().getScaledHeight() - height - 8 : ClientConfig.hudInfoY();
        drawPanel(context, x, y, width, height, accent);
        context.fill(x + 8, y + 8, x + 36, y + 36, 0xFF15121A);
        context.fill(x + 8, y + 8, x + 10, y + 36, accent);
        context.drawText(client.textRenderer, Text.literal("Z"), x + 18, y + 15, accent, true);
        context.drawText(client.textRenderer, Text.literal("TELIKINESDLC"), x + 46, y + 7, 0xFFF5EFF7, true);
        context.drawText(client.textRenderer, Text.literal("1.21.11  •  " + ClientConfig.plan()), x + 46, y + 21, accent, false);
        int fps = client.getCurrentFps(), ping = getPing(client);
        context.drawText(client.textRenderer, Text.literal("FPS " + fps), x + 9, y + 45, 0xFFD7CFDB, false);
        context.drawText(client.textRenderer, Text.literal("PING " + (ping >= 0 ? ping + "ms" : "--")), x + 78, y + 45, 0xFFD7CFDB, false);
        String coords = String.format("XYZ %d %d %d", client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
        context.drawText(client.textRenderer, Text.literal(coords), x + 9, y + 58, 0xFFAFA5B3, false);
        context.drawText(client.textRenderer, Text.literal(LocalTime.now().format(CLOCK)), x + width - 61, y + 58, accent, false);
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
                .sorted(Comparator.comparing(entry -> entry.getProfile().name())).toList();
        if (staff.isEmpty()) return;
        int x = ClientConfig.hudStaffX(), y = ClientConfig.hudStaffY();
        int width = 202, height = 31 + Math.min(staff.size(), 6) * 16;
        drawPanel(context, x, y, width, height, accent);
        drawTitle(context, client, "STAFF LIST", x, y);
        int rowY = y + 23;
        for (PlayerListEntry entry : staff) {
            if (rowY > y + height - 8) break;
            context.drawText(client.textRenderer, Text.literal(entry.getProfile().name()), x + 9, rowY, 0xFFD8CFDD, false);
            context.drawText(client.textRenderer, Text.literal("ONLINE"), x + width - 52, rowY, accent, false);
            rowY += 16;
        }
    }
}
