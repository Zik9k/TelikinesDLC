package com.zik9k.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class CosmeticsClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zik9k.cosmetics",
                net.minecraft.client.util.InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.MISC
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new WardrobeScreen());
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(CosmeticsClient::renderWorldCosmetics);
    }

    private static void renderWorldCosmetics(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        PlayerEntity player = client.player;
        Vec3d p = player.getEntityPos().add(0.0, 0.02, 0.0);
        Vec3d camera = context.worldState().cameraRenderState.pos;
        if (camera.squaredDistanceTo(p) > 64.0 * 64.0) return;

        if (CosmeticsState.isEquipped("crown")) drawCrown(p);
        if (CosmeticsState.isEquipped("crimson_wings")) drawWings(p, player.getBodyYaw());
        if (CosmeticsState.isEquipped("blue_aura")) drawAura(p);
    }

    private static void drawCrown(Vec3d p) {
        int gold = ColorHelper.fromFloats(1.0f, 0.72f, 0.12f, 0.95f);
        GizmoDrawing.box(new Box(p.x - 0.24, p.y + 2.02, p.z - 0.20, p.x + 0.24, p.y + 2.14, p.z + 0.20), DrawStyle.filled(gold));
        GizmoDrawing.box(new Box(p.x - 0.22, p.y + 2.14, p.z - 0.18, p.x - 0.10, p.y + 2.32, p.z + 0.02), DrawStyle.filled(gold));
        GizmoDrawing.box(new Box(p.x - 0.04, p.y + 2.14, p.z - 0.18, p.x + 0.08, p.y + 2.38, p.z + 0.02), DrawStyle.filled(gold));
        GizmoDrawing.box(new Box(p.x + 0.14, p.y + 2.14, p.z - 0.18, p.x + 0.26, p.y + 2.30, p.z + 0.02), DrawStyle.filled(gold));
    }

    private static void drawWings(Vec3d p, float bodyYaw) {
        float yaw = (float) Math.toRadians(bodyYaw);
        double bx = -Math.sin(yaw) * 0.42;
        double bz = -Math.cos(yaw) * 0.42;
        int red = ColorHelper.fromFloats(0.72f, 0.08f, 0.10f, 0.90f);
        int dark = ColorHelper.fromFloats(0.10f, 0.03f, 0.05f, 0.92f);
        for (int side : new int[]{-1, 1}) {
            double sx = Math.cos(yaw) * side * 0.32;
            double sz = -Math.sin(yaw) * side * 0.32;
            GizmoDrawing.box(new Box(p.x + bx + sx - 0.12, p.y + 0.82, p.z + bz + sz - 0.08,
                    p.x + bx + sx + 0.12, p.y + 1.55, p.z + bz + sz + 0.08), DrawStyle.filled(dark));
            GizmoDrawing.box(new Box(p.x + bx + sx - 0.20, p.y + 1.05, p.z + bz + sz - 0.07,
                    p.x + bx + sx + 0.20, p.y + 1.22, p.z + bz + sz + 0.07), DrawStyle.filled(red));
        }
    }

    private static void drawAura(Vec3d p) {
        int blue = ColorHelper.fromFloats(0.15f, 0.55f, 1.0f, 0.34f);
        double s = 0.55;
        GizmoDrawing.box(new Box(p.x - s, p.y + 0.05, p.z - s, p.x + s, p.y + 1.95, p.z + s), DrawStyle.filled(blue));
    }

    private enum Category {
        HAT("Шляпа"), FACE("Лицо"), BACK("Спина"), WINGS("Крылья"), EFFECT("Эффект"), EMOTE("Эмоция"), PET("Питомец");
        final String title;
        Category(String title) { this.title = title; }
    }

    private record Cosmetic(String id, String name, Category category, int color, boolean unlocked) {}

    private static final List<Cosmetic> COSMETICS = List.of(
            new Cosmetic("crown", "Корона", Category.HAT, 0xFFFFC21C, true),
            new Cosmetic("gold_halo", "Золотой нимб", Category.HAT, 0xFFFFE477, false),
            new Cosmetic("mask", "Черная маска", Category.FACE, 0xFF24242A, false),
            new Cosmetic("cape", "Алый плащ", Category.BACK, 0xFF9D1C2B, false),
            new Cosmetic("crimson_wings", "Красно-черные крылья", Category.WINGS, 0xFF9F1C2B, true),
            new Cosmetic("blue_aura", "Синяя аура", Category.EFFECT, 0xFF2E8BFF, true),
            new Cosmetic("spark", "Искры", Category.EFFECT, 0xFFB7ECFF, false),
            new Cosmetic("wave", "Эмоция: привет", Category.EMOTE, 0xFFB15CFF, false),
            new Cosmetic("mini_dragon", "Питомец: дракон", Category.PET, 0xFF5A35A8, false)
    );

    private static final class CosmeticsState {
        private static final List<String> equipped = new ArrayList<>();
        static boolean isEquipped(String id) { return equipped.contains(id); }
        static void toggle(String id) { if (!equipped.remove(id)) equipped.add(id); }
        static void clear() { equipped.clear(); }
    }

    private static final class WardrobeScreen extends Screen {
        private int selected = 0;
        private String search = "";
        private boolean searchFocused;
        private long lastBlink;
        private boolean cursorVisible = true;

        protected WardrobeScreen() { super(Text.literal("Гардероб")); }

        @Override protected void init() {
            searchFocused = false;
            lastBlink = System.currentTimeMillis();
        }

        private int left() { return (width - 860) / 2; }
        private int top() { return (height - 520) / 2; }
        private Category category() { return Category.values()[selected]; }
        private List<Cosmetic> filtered() {
            String q = search.toLowerCase();
            return COSMETICS.stream().filter(c -> c.category == category()).filter(c -> q.isBlank() || c.name.toLowerCase().contains(q)).toList();
        }

        @Override public void render(DrawContext c, int mx, int my, float delta) {
            int l = left(), t = top(), r = l + 860, b = t + 520;
            c.fill(0, 0, width, height, 0x99000000);
            c.fill(l + 8, t + 10, r + 8, b + 10, 0x50000000);
            c.fill(l, t, r, b, 0xFF17131F);
            c.fill(l, t, l + 170, b, 0xFF120F18);
            c.drawText(textRenderer, Text.literal("Гардероб"), l + 22, t + 18, 0xFFF5EDF9, false);
            c.drawText(textRenderer, Text.literal("2 вещей экипировано, 1 не открыто"), l + 22, t + 38, 0xFF8E8494, false);

            int currencyX = r - 94;
            c.fill(currencyX, t + 12, r - 16, t + 38, 0xFF24202A);
            c.drawText(textRenderer, Text.literal("0  ◆"), currencyX + 15, t + 20, 0xFFFFC95E, false);

            for (int i = 0; i < Category.values().length; i++) {
                int yy = t + 72 + i * 42;
                boolean sel = i == selected;
                if (sel) c.fill(l + 12, yy - 5, l + 158, yy + 30, 0xFF2A2036);
                c.drawText(textRenderer, Text.literal(Category.values()[i].title), l + 28, yy + 7, sel ? 0xFFFFFFFF : 0xFF908695, false);
            }

            int contentLeft = l + 184;
            int searchL = contentLeft;
            int searchR = contentLeft + 325;
            c.fill(searchL, t + 54, searchR, t + 82, searchFocused ? 0xFF302838 : 0xFF25202B);
            String vis = search.length() > 24 ? search.substring(0, 24) : search;
            c.drawText(textRenderer, Text.literal(vis.isBlank() ? "Поиск" : vis), searchL + 10, t + 63, vis.isBlank() ? 0xFF756A78 : 0xFFE8E0EA, false);
            if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
                c.fill(searchL + 10 + textRenderer.getWidth(vis), t + 61, searchL + 11 + textRenderer.getWidth(vis), t + 75, 0xFFE8E0EA);
            }

            List<Cosmetic> items = filtered();
            int cardW = 142, cardH = 132, gap = 10;
            int startY = t + 96;
            for (int i = 0; i < items.size(); i++) {
                int col = i % 3, row = i / 3;
                int x = contentLeft + col * (cardW + gap);
                int y = startY + row * (cardH + gap);
                if (y + cardH > b - 58) break;
                drawCard(c, items.get(i), x, y, cardW, cardH, mx, my);
            }

            int previewL = l + 660;
            c.fill(previewL, t + 54, r - 18, b - 54, 0xFF111017);
            c.drawText(textRenderer, Text.literal("Предпросмотр"), previewL + 18, t + 70, 0xFFDAD2DD, false);
            drawPreview(c, previewL + 18, t + 100, 160, 260);

            c.fill(l + 184, b - 42, l + 324, b - 16, 0xFF2A2230);
            c.drawText(textRenderer, Text.literal("Снять всё"), l + 224, b - 33, 0xFFE8E0EA, false);
            c.fill(r - 138, b - 42, r - 18, b - 16, 0xFF38213A);
            c.drawText(textRenderer, Text.literal("Закрыть"), r - 104, b - 33, 0xFFF5E8F6, false);
            super.render(c, mx, my, delta);
        }

        private void drawCard(DrawContext c, Cosmetic item, int x, int y, int w, int h, int mx, int my) {
            boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
            boolean equipped = CosmeticsState.isEquipped(item.id);
            c.fill(x, y, x + w, y + h, hover ? 0xFF2A2330 : 0xFF1C1821);
            int col = item.color;
            c.fill(x + 18, y + 14, x + 58, y + 54, col);
            c.drawText(textRenderer, Text.literal(item.name), x + 10, y + 69, 0xFFEAE2EC, false);
            if (!item.unlocked) {
                c.fill(x + w - 36, y + 12, x + w - 14, y + 34, 0xCC3E3844);
                c.drawText(textRenderer, Text.literal("🔒"), x + w - 34, y + 17, 0xFFE8E0EA, false);
            } else {
                String state = equipped ? "НАДЕТО" : "НАДЕТЬ";
                c.drawText(textRenderer, Text.literal(state), x + 10, y + 95, equipped ? 0xFF69E6A6 : 0xFF9D8BA5, false);
            }
        }

        private void drawPreview(DrawContext c, int x, int y, int w, int h) {
            int cx = x + w / 2;
            c.fill(cx - 28, y + 50, cx + 28, y + 120, 0xFF4A7C59);
            c.fill(cx - 18, y + 24, cx + 18, y + 58, 0xFFC89A7B);
            c.fill(cx - 34, y + 118, cx - 6, y + 205, 0xFF31343F);
            c.fill(cx + 6, y + 118, cx + 34, y + 205, 0xFF31343F);
            if (CosmeticsState.isEquipped("crown")) {
                c.fill(cx - 22, y + 12, cx + 22, y + 24, 0xFFFFC21C);
                c.fill(cx - 16, y + 2, cx - 7, y + 14, 0xFFFFC21C);
                c.fill(cx - 3, y - 2, cx + 6, y + 14, 0xFFFFC21C);
                c.fill(cx + 10, y + 1, cx + 19, y + 14, 0xFFFFC21C);
            }
            if (CosmeticsState.isEquipped("crimson_wings")) {
                c.fill(cx - 72, y + 72, cx - 34, y + 130, 0xFF7F1D2B);
                c.fill(cx + 34, y + 72, cx + 72, y + 130, 0xFF7F1D2B);
                c.fill(cx - 84, y + 96, cx - 38, y + 122, 0xFFBE2B3A);
                c.fill(cx + 38, y + 96, cx + 84, y + 122, 0xFFBE2B3A);
            }
        }

        private int itemAt(double mx, double my) {
            List<Cosmetic> items = filtered();
            int l = left(), t = top(), contentLeft = l + 184, cardW = 142, cardH = 132, gap = 10, startY = t + 96;
            for (int i = 0; i < items.size(); i++) {
                int col = i % 3, row = i / 3;
                int x = contentLeft + col * (cardW + gap);
                int y = startY + row * (cardH + gap);
                if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) return i;
            }
            return -1;
        }

        @Override public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
            double mx = click.x(), my = click.y();
            int button = click.button();
            int l = left(), t = top(), r = l + 860, b = t + 520;
            if (button == 0) {
                for (int i = 0; i < Category.values().length; i++) {
                    int yy = t + 72 + i * 42;
                    if (mx >= l + 12 && mx <= l + 158 && my >= yy - 5 && my <= yy + 30) { selected = i; searchFocused = false; return true; }
                }
                if (mx >= l + 184 && mx <= l + 509 && my >= t + 54 && my <= t + 82) { searchFocused = true; return true; }
                int idx = itemAt(mx, my);
                if (idx >= 0) {
                    Cosmetic item = filtered().get(idx);
                    if (item.unlocked) CosmeticsState.toggle(item.id);
                    return true;
                }
                if (mx >= l + 184 && mx <= l + 324 && my >= b - 42 && my <= b - 16) { CosmeticsState.clear(); return true; }
                if (mx >= r - 138 && mx <= r - 18 && my >= b - 42 && my <= b - 16) { close(); return true; }
            }
            return super.mouseClicked(click, doubled);
        }

        @Override public boolean charTyped(CharInput input) {
            if (searchFocused && input.isValidChar() && search.length() < 48) { search += input.asString(); return true; }
            return super.charTyped(input);
        }

        @Override public boolean keyPressed(KeyInput input) {
            int key = input.key();
            if (searchFocused) {
                if (key == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
                if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length() - 1); return true; }
                if (key == GLFW.GLFW_KEY_DELETE || (key == GLFW.GLFW_KEY_A && (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0)) { search = ""; return true; }
            }
            return super.keyPressed(input);
        }

        @Override public boolean shouldPause() { return false; }
    }
}
