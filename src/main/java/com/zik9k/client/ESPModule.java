package com.zik9k.client;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class ESPModule extends Module {
    private boolean players = true;
    private boolean mobs = true;
    private boolean animals = false;
    private int range = 64;

    public ESPModule() {
        super("ESP", "Filled entity hitboxes without the direction arrow", ModuleCategory.RENDER);
        loadSettings();
    }

    boolean isSelected(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return players;
        if (entity instanceof AnimalEntity) return animals;
        return mobs;
    }

    float[] colorFor(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return new float[]{0.70f, 0.35f, 0.95f, 0.28f};
        if (entity instanceof AnimalEntity) return new float[]{0.35f, 0.90f, 0.55f, 0.24f};
        return new float[]{0.95f, 0.35f, 0.35f, 0.24f};
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
