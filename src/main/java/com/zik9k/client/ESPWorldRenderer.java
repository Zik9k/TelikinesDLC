package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        Vec3d cameraPos = context.camera().getCameraPos();
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getDebugFilledBox());

        context.matrices().push();
        context.matrices().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) continue;
            if (!esp.isSelected(living)) continue;
            if (cameraPos.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) > (double) esp.range() * esp.range()) continue;

            Box box = entity.getBoundingBox().expand(0.015D);
            float[] color = esp.colorFor(living);
            VertexRendering.drawFilledBox(
                    context.matrices(), consumer,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    color[0], color[1], color[2], color[3]
            );
        }

        context.matrices().pop();
    }
}
