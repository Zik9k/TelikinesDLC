package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
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
        if (client.world == null || client.player == null) return;

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

        float partial = client.getRenderTickCounter().getTickProgress(false);
        Vec3d center = target.getLerpedPos(partial).add(0.0D, target.getHeight() * 0.58D, 0.0D);
        Camera camera = client.gameRenderer.getCamera();

        float cooldown = client.player.getAttackCooldownProgress(partial);
        if (cooldown < previousCooldown - 0.20F) {
            attackFlashUntil = System.currentTimeMillis() + 150L;
        }
        previousCooldown = cooldown;
        boolean hitFlash = target.hurtTime > 0 || System.currentTimeMillis() < attackFlashUntil;

        int accent = switch (ClientConfig.accent()) {
            case 1 -> 0xFF8D67FF;
            case 2 -> 0xFFE26BFF;
            default -> 0xFFB15CFF;
        };
        float r = hitFlash ? 1.0F : ((accent >> 16) & 0xFF) / 255.0F;
        float g = hitFlash ? 0.10F : ((accent >> 8) & 0xFF) / 255.0F;
        float b = hitFlash ? 0.10F : (accent & 0xFF) / 255.0F;
        int packedColor = ColorHelper.fromFloats(r, g, b, 0.96F);

        Vector3fc horizontal = camera.getHorizontalPlane();
        Vector3fc vertical = camera.getVerticalPlane();
        Vec3d right = new Vec3d(horizontal.x(), horizontal.y(), horizontal.z());
        Vec3d up = new Vec3d(vertical.x(), vertical.y(), vertical.z());

        double angle = (System.currentTimeMillis() % 3600L) / 3600.0D * Math.PI * 2.0D;
        double size = Math.max(0.27D, Math.min(0.50D, target.getWidth() * 0.28D + 0.16D));
        double segment = size * 0.46D;

        Vec3d u = right.multiply(Math.cos(angle)).add(up.multiply(Math.sin(angle)));
        Vec3d v = up.multiply(Math.cos(angle)).subtract(right.multiply(Math.sin(angle)));

        drawCorner(center, u, v, size, segment, 1, 1, packedColor);
        drawCorner(center, u, v, size, segment, -1, 1, packedColor);
        drawCorner(center, u, v, size, segment, 1, -1, packedColor);
        drawCorner(center, u, v, size, segment, -1, -1, packedColor);
    }

    private static void drawCorner(Vec3d center, Vec3d u, Vec3d v, double size, double segment,
                                   double sx, double sy, int color) {
        Vec3d corner = center.add(u.multiply(sx * size)).add(v.multiply(sy * size));
        Vec3d alongU = corner.add(u.multiply(-sx * segment));
        Vec3d alongV = corner.add(v.multiply(-sy * segment));
        GizmoDrawing.line(corner, alongU, color, 2.0F);
        GizmoDrawing.line(corner, alongV, color, 2.0F);
    }
}
