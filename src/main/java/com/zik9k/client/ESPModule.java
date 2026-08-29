package com.zik9k.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MatrixStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ESPModule extends Module {
    private boolean players = true;
    private boolean mobs = true;
    private boolean animals = false;
    private int range = 64;
    private static boolean rendererRegistered;

    public ESPModule() {
        super("ESP", "Filled hitbox-style boxes around selected living entities", ModuleCategory.RENDER);
        loadSettings();
    }

    public static void registerRenderer() {
        if (rendererRegistered) return;
        rendererRegistered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(ESPModule::renderWorld);
    }

    private static void renderWorld(WorldRenderContext context) {
        Module module = ModuleManager.getModules(ModuleCategory.RENDER).stream()
                .filter(candidate -> candidate instanceof ESPModule)
                .findFirst()
                .orElse(null);
        if (!(module instanceof ESPModule esp) || !esp.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        MatrixStack matrices = context.matrices();
        Vec3d camera = context.camera().getCameraPos();
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getDebugFilledBox());

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == client.player || !living.isAlive()) continue;
            if (!esp.isSelected(living)) continue;
            if (client.player.squaredDistanceTo(entity) > (double) esp.range * esp.range) continue;

            Box box = entity.getBoundingBox().expand(0.02D);
            drawFilledBox(matrices, consumer, box, 0x8CB15CFF);
        }

        matrices.pop();
    }

    private static void drawFilledBox(MatrixStack matrices, VertexConsumer consumer, Box box, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        MatrixStack.Entry entry = matrices.peek();

        quad(consumer, entry, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, a);
        quad(consumer, entry, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        quad(consumer, entry, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a);
        quad(consumer, entry, box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        quad(consumer, entry, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, r, g, b, a);
        quad(consumer, entry, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
    }

    private static void quad(VertexConsumer consumer, MatrixStack.Entry entry,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             int r, int g, int b, int a) {
        float minX = (float) Math.min(x1, x2);
        float maxX = (float) Math.max(x1, x2);
        float minY = (float) Math.min(y1, y2);
        float maxY = (float) Math.max(y1, y2);
        float minZ = (float) Math.min(z1, z2);
        float maxZ = (float) Math.max(z1, z2);

        if (x1 == x2) {
            vertex(consumer, entry, minX, minY, minZ, r, g, b, a);
            vertex(consumer, entry, minX, minY, maxZ, r, g, b, a);
            vertex(consumer, entry, minX, maxY, maxZ, r, g, b, a);
            vertex(consumer, entry, minX, maxY, minZ, r, g, b, a);
        } else if (y1 == y2) {
            vertex(consumer, entry, minX, minY, minZ, r, g, b, a);
            vertex(consumer, entry, maxX, minY, minZ, r, g, b, a);
            vertex(consumer, entry, maxX, minY, maxZ, r, g, b, a);
            vertex(consumer, entry, minX, minY, maxZ, r, g, b, a);
        } else {
            vertex(consumer, entry, minX, minY, minZ, r, g, b, a);
            vertex(consumer, entry, minX, maxY, minZ, r, g, b, a);
            vertex(consumer, entry, maxX, maxY, minZ, r, g, b, a);
            vertex(consumer, entry, maxX, minY, minZ, r, g, b, a);
        }
    }

    private static void vertex(VertexConsumer consumer, MatrixStack.Entry entry,
                               float x, float y, float z, int r, int g, int b, int a) {
        consumer.vertex(entry, x, y, z).color(r, g, b, a);
    }

    private boolean isSelected(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return players;
        if (entity instanceof AnimalEntity) return animals;
        return mobs;
    }

    private void loadSettings() {
        players = ClientConfig.espPlayers();
        mobs = ClientConfig.espMobs();
        animals = ClientConfig.espAnimals();
        range = ClientConfig.espRange();
    }

    public boolean players() { return players; }
    public boolean mobs() { return mobs; }
    public boolean animals() { return animals; }
    public int range() { return range; }
    public void setPlayers(boolean value) { players = value; ClientConfig.setEspPlayers(value); }
    public void setMobs(boolean value) { mobs = value; ClientConfig.setEspMobs(value); }
    public void setAnimals(boolean value) { animals = value; ClientConfig.setEspAnimals(value); }
    public void setRange(int value) { range = Math.max(8, Math.min(128, value)); ClientConfig.setEspRange(range); }
}
