package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

/** Draws a translucent filled box over the block under the crosshair. */
public final class BlockOverlayWorldRenderer {
    private BlockOverlayWorldRenderer() { }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockOverlayWorldRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        BlockOverlayModule module = null;
        for (Module candidate : ModuleManager.getModules()) {
            if (candidate instanceof BlockOverlayModule overlay) {
                module = overlay;
                break;
            }
        }
        if (module == null || !module.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || !(client.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        Box box = new Box(pos).expand(0.002D);
        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();
        if (camera.squaredDistanceTo(Vec3d.ofCenter(pos)) > 128.0D * 128.0D) return;

        float r = 0.70f, g = 0.35f, b = 0.95f;
        GizmoDrawing.box(box, DrawStyle.filled(ColorHelper.fromFloats(r, g, b, module.alpha() / 255.0f)));
    }
}
