package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

/** Renders a rotating diamond over the entity currently targeted by aim or Kill Aura. */
public final class TargetEspWorldRenderer {
    private TargetEspWorldRenderer() { }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TargetEspWorldRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || context.consumers() == null) return;

        LivingEntity target = null;
        for (Module module : ModuleManager.getModules()) {
            if (module instanceof KillAuraModule aura && aura.isEnabled()) {
                target = aura.currentTarget();
                if (target != null) break;
            }
        }

        if (target == null && client.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living && living != client.player) {
            target = living;
        }
        if (target == null || !target.isAlive()) return;
        if (client.player.squaredDistanceTo(target) > 64.0D * 64.0D) return;

        float partial = context.tickCounter().getTickProgress(false);
        Vec3d pos = target.getLerpedPos(partial).add(0.0D, target.getHeight() * 0.62D, 0.0D);
        Vec3d camera = context.camera().getCameraPos();
        float time = (System.currentTimeMillis() % 5000L) / 1000.0f;
        double angle = time * 1.7D;
        double radius = Math.max(0.45D, Math.min(0.85D, target.getWidth() * 0.45D + 0.25D));
        double height = Math.max(0.45D, target.getHeight() * 0.32D);

        context.matrices().push();
        context.matrices().translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());

        Vec3d top = pos.add(0.0D, height, 0.0D);
        Vec3d bottom = pos.add(0.0D, -height, 0.0D);
        Vec3d[] ring = new Vec3d[4];
        for (int i = 0; i < 4; i++) {
            double a = angle + i * Math.PI / 2.0D;
            ring[i] = pos.add(Math.cos(a) * radius, 0.0D, Math.sin(a) * radius);
        }

        for (int i = 0; i < 4; i++) {
            line(context, consumer, top, ring[i], 0.78f, 0.42f, 1.0f, 0.95f);
            line(context, consumer, bottom, ring[i], 0.78f, 0.42f, 1.0f, 0.95f);
            line(context, consumer, ring[i], ring[(i + 1) % 4], 0.78f, 0.42f, 1.0f, 0.95f);
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
