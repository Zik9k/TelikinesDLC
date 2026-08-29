package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.debug.gizmo.DrawStyle;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

/** Draws ESP as translucent filled entity hitboxes, with no direction arrow. */
public final class ESPWorldRenderer {
    private ESPWorldRenderer() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(ESPWorldRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        ESPModule esp = null;
        for (Module module : ModuleManager.getModules()) {
            if (module instanceof ESPModule candidate) {
                esp = candidate;
                break;
            }
        }
        if (esp == null || !esp.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Vec3dAccessor camera = new Vec3dAccessor(context.camera().getCameraPos().x, context.camera().getCameraPos().y, context.camera().getCameraPos().z);
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) continue;
            if (!esp.isSelected(living)) continue;
            if (camera.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) > (double) esp.range() * esp.range()) continue;

            Box box = entity.getBoundingBox().expand(0.015D);
            float[] color = esp.colorFor(living);
            GizmoDrawing.box(box, DrawStyle.filled(ColorHelper.fromFloats(color[0], color[1], color[2], color[3])));
        }
    }

    private record Vec3dAccessor(double x, double y, double z) {
        double squaredDistanceTo(double ox, double oy, double oz) {
            double dx = x - ox;
            double dy = y - oy;
            double dz = z - oz;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
