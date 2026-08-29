package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class TracersModule extends Module {
    private boolean players = true;
    private boolean mobs = true;
    private boolean animals = false;
    private int range = 64;

    public TracersModule() {
        super("Tracers", "Draws lines from you to selected living entities", ModuleCategory.RENDER);
        loadSettings();
    }

    boolean isSelected(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return players;
        if (entity instanceof AnimalEntity) return animals;
        return mobs;
    }

    float[] colorFor(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return new float[]{0.70f, 0.35f, 0.95f, 0.90f};
        if (entity instanceof AnimalEntity) return new float[]{0.35f, 0.90f, 0.55f, 0.80f};
        return new float[]{0.95f, 0.35f, 0.35f, 0.80f};
    }

    private void loadSettings() {
        players = ClientConfig.tracersPlayers();
        mobs = ClientConfig.tracersMobs();
        animals = ClientConfig.tracersAnimals();
        range = ClientConfig.tracersRange();
    }

    public boolean players() { return players; }
    public boolean mobs() { return mobs; }
    public boolean animals() { return animals; }
    public int range() { return range; }

    public void setPlayers(boolean value) { players = value; ClientConfig.setTracersPlayers(value); }
    public void setMobs(boolean value) { mobs = value; ClientConfig.setTracersMobs(value); }
    public void setAnimals(boolean value) { animals = value; ClientConfig.setTracersAnimals(value); }
    public void setRange(int value) { range = Math.max(8, Math.min(128, value)); ClientConfig.setTracersRange(range); }
}
