package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class ESPModule extends Module {
    private boolean players = true;
    private boolean mobs = true;
    private boolean animals = false;
    private int range = 64;

    public ESPModule() {
        super("ESP", "Highlights selected living entities through the vanilla outline renderer", ModuleCategory.RENDER);
        loadSettings();
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == player) continue;
            if (!isSelected(living) || player.squaredDistanceTo(entity) > (double) range * range) {
                continue;
            }
            living.setGlowing(true);
        }
    }

    private boolean isSelected(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return players;
        if (entity instanceof AnimalEntity) return animals;
        return mobs;
    }

    @Override
    protected void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity living && !(living instanceof ClientPlayerEntity)) {
                living.setGlowing(false);
            }
        }
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
