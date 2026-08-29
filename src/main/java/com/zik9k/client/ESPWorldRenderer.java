package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Renders the ESP as a translucent filled entity hitbox without the vanilla direction arrow. */
public final class ESPWorldRenderer {
    private ESPWorldRenderer() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(ESPWorldRenderer::render);
    }

    private static void render(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        ESPModule esp = getESP();
        if (esp == null || !esp.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        float tickProgress = context.tickCounter().getTickProgress(false);
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getDebugFilledBox());

        context.matrices().push();
        context.matrices().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) continue;
            if (!esp.isSelected(living)) continue;

            Vec3d pos = entity.getLerpedPos(tickProgress);
            Box box = entity.getBoundingBox();
            double dx = pos.x - entity.getX();
            double dy = pos.y - entity.getY();
            double dz = pos.z - entity.getZ();
            Box renderBox = box.offset(dx, dy, dz).expand(0.02D);

            if (cameraPos.squaredDistanceTo(pos) > (double) esp.range() * esp.range()) continue;

            float[] color = esp.colorFor(living);
            VertexRendering.drawFilledBox(
                    context.matrices(),
                    consumer,
                    renderBox.minX,
                    renderBox.minY,
                    renderBox.minZ,
                    renderBox.maxX,
                    renderBox.maxY,
                    renderBox.maxZ,
                    color[0], color[1], color[2], color[3]
            );
        }

        context.matrices().pop();
    }

    private static ESPModule getESP() {
        for (Module module : ModuleManager.getModules()) {
            if (module instanceof ESPModule esp) return esp;
        }
        return null;
    }
}
