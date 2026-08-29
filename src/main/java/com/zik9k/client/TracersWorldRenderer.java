package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

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
        VertexConsumer consumers = context.consumers() == null ? null : context.consumers().getBuffer(RenderLayers.lines());
        if (consumers == null) return;

        Vec3d cameraPos = context.camera().getCameraPos();
        float tickProgress = context.tickCounter().getTickProgress(false);
        Vec3d start = client.player.getLerpedPos(tickProgress).add(0.0, client.player.getStandingEyeHeight() * 0.5, 0.0);
        context.matrices().push();
        context.matrices().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || living == client.player) continue;
            if (!module.isSelected(living)) continue;
            Vec3d pos = entity.getLerpedPos(tickProgress).add(0.0, living.getHeight() * 0.5, 0.0);
            if (cameraPos.squaredDistanceTo(pos) > (double) module.range() * module.range()) continue;

            float[] color = module.colorFor(living);
            line(context, consumers, start, pos, color[0], color[1], color[2], color[3]);
        }

        context.matrices().pop();
    }

    private static void line(WorldRenderContext context, VertexConsumer consumer, Vec3d from, Vec3d to,
                             float r, float g, float b, float a) {
        Vec3d normal = to.subtract(from).normalize();
        consumer.vertex(context.matrices().peek(), (float) from.x, (float) from.y, (float) from.z)
                .color(r, g, b, a).normal((float) normal.x, (float) normal.y, (float) normal.z);
        consumer.vertex(context.matrices().peek(), (float) to.x, (float) to.y, (float) to.z)
                .color(r, g, b, a).normal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
