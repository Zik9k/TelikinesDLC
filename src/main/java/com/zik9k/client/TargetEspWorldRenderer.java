package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;

/** Camera-facing rotating bracket target marker for aim and Kill Aura targets. */
public final class TargetEspWorldRenderer {
    private static long attackFlashUntil;
    private static float previousCooldown = 1.0F;

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
        Vec3d center = target.getLerpedPos(partial).add(0.0D, target.getHeight() * 0.58D, 0.0D);
        Vec3d camera = context.camera().getCameraPos();

        float cooldown = client.player.getAttackCooldownProgress(partial);
        if (cooldown < previousCooldown - 0.20F) {
            attackFlashUntil = System.currentTimeMillis() + 150L;
        }
        previousCooldown = cooldown;
        boolean hitFlash = client.options.attackKey.isPressed() || System.currentTimeMillis() < attackFlashUntil;

        int accent = switch (ClientConfig.accent()) {
            case 1 -> 0xFF8D67FF;
            case 2 -> 0xFFE26BFF;
            default -> 0xFFB15CFF;
        };
        float r = hitFlash ? 1.0F : ((accent >> 16) & 0xFF) / 255.0F;
        float g = hitFlash ? 0.16F : ((accent >> 8) & 0xFF) / 255.0F;
        float b = hitFlash ? 0.16F : (accent & 0xFF) / 255.0F;

        Vector3fc horizontal = context.camera().getHorizontalPlane();
        Vector3fc vertical = context.camera().getVerticalPlane();
        Vec3d right = new Vec3d(horizontal.x(), horizontal.y(), horizontal.z());
        Vec3d up = new Vec3d(vertical.x(), vertical.y(), vertical.z());

        double angle = (System.currentTimeMillis() % 3600L) / 3600.0D * Math.PI * 2.0D;
        double size = Math.max(0.27D, Math.min(0.50D, target.getWidth() * 0.28D + 0.16D));
        double segment = size * 0.46D;

        Vec3d u = right.multiply(Math.cos(angle)).add(up.multiply(Math.sin(angle)));
        Vec3d v = up.multiply(Math.cos(angle)).subtract(right.multiply(Math.sin(angle)));

        context.matrices().push();
        context.matrices().translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());

        drawCorner(context, consumer, center, u, v, size, segment, 1, 1, r, g, b);
        drawCorner(context, consumer, center, u, v, size, segment, -1, 1, r, g, b);
        drawCorner(context, consumer, center, u, v, size, segment, 1, -1, r, g, b);
        drawCorner(context, consumer, center, u, v, size, segment, -1, -1, r, g, b);

        context.matrices().pop();
    }

    private static void drawCorner(WorldRenderContext context, VertexConsumer consumer, Vec3d center,
                                   Vec3d u, Vec3d v, double size, double segment,
                                   double sx, double sy, float r, float g, float b) {
        Vec3d corner = center.add(u.multiply(sx * size)).add(v.multiply(sy * size));
        Vec3d alongU = corner.add(u.multiply(-sx * segment));
        Vec3d alongV = corner.add(v.multiply(-sy * segment));
        line(context, consumer, corner, alongU, r, g, b, 0.96F);
        line(context, consumer, corner, alongV, r, g, b, 0.96F);
    }

    private static void line(WorldRenderContext context, VertexConsumer consumer, Vec3d from, Vec3d to,
                             float r, float g, float b, float a) {
        Vec3d normal = context.camera().getCameraPos().subtract(from).normalize();
        consumer.vertex(context.matrices().peek(), (float) from.x, (float) from.y, (float) from.z)
                .color(r, g, b, a).normal((float) normal.x, (float) normal.y, (float) normal.z);
        consumer.vertex(context.matrices().peek(), (float) to.x, (float) to.y, (float) to.z)
                .color(r, g, b, a).normal((float) normal.x, (float) normal.y, (float) normal.z);
    }
}
