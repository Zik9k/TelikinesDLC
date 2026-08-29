package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Draws a translucent filled and outlined box over the block under the crosshair. */
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

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        BlockPos pos = hit.getBlockPos();
        Box box = new Box(pos).expand(0.002D);
        Vec3d camera = context.camera().getCameraPos();
        float r = 0.70f, g = 0.35f, b = 0.95f;

        context.matrices().push();
        context.matrices().translate(-camera.x, -camera.y, -camera.z);

        VertexConsumer fillConsumer = consumers.getBuffer(RenderLayer.getDebugFilledBox());
        VertexRendering.drawFilledBox(
                context.matrices(), fillConsumer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, module.alpha() / 255.0f
        );

        VertexConsumer outlineConsumer = consumers.getBuffer(RenderLayer.getLines());
        WorldRenderer.drawBox(
                context.matrices(), outlineConsumer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, module.outlineAlpha() / 255.0f
        );
        context.matrices().pop();
    }
}
