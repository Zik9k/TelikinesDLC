package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

/** Draws tracer lines from the local player to selected entities. */
public final class TracersWorldRenderer {
    private TracersWorldRenderer() { }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TracersWorldRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        TracersModule module = null;
        for (Module candidate : ModuleManager.getModules()) {
            if (candidate instanceof TracersModule tracers) {
                module = tracers;
                break;
            }
        }
        if (module == null || !module.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        float tickProgress = client.getRenderTickCounter().getTickProgress(false);
        Vec3d start = client.player.getLerpedPos(tickProgress)
                .add(0.0, client.player.getStandingEyeHeight() * 0.5, 0.0);

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) continue;
            if (!module.isSelected(living)) continue;

            Vec3d pos = entity.getLerpedPos(tickProgress)
                    .add(0.0, living.getHeight() * 0.5, 0.0);
            if (start.squaredDistanceTo(pos) > (double) module.range() * module.range()) continue;

            float[] color = module.colorFor(living);
            int packedColor = ColorHelper.fromFloats(color[0], color[1], color[2], color[3]);
            GizmoDrawing.line(start, pos, packedColor, 1.5F);
        }
    }
}"}