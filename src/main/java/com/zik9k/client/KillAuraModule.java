package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;

public final class KillAuraModule extends Module {
    private boolean players = true;
    private boolean mobs = true;
    private boolean animals = false;
    private int range = 4;
    private int cps = 8;
    private long nextAttackTime;

    public KillAuraModule() {
        super("Kill Aura", "Automatically attacks nearby selected living entities", ModuleCategory.COMBAT);
        players = ClientConfig.killAuraPlayers();
        mobs = ClientConfig.killAuraMobs();
        animals = ClientConfig.killAuraAnimals();
        range = ClientConfig.killAuraRange();
        cps = ClientConfig.killAuraCps();
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null || client.currentScreen != null) return;

        if (System.currentTimeMillis() < nextAttackTime) return;
        if (player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        LivingEntity target = client.world.getEntitiesByClass(
                        LivingEntity.class,
                        player.getBoundingBox().expand(range),
                        this::isValidTarget
                ).stream()
                .min(Comparator.comparingDouble(player::squaredDistanceTo))
                .orElse(null);

        if (target == null) return;
        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        nextAttackTime = System.currentTimeMillis() + Math.max(1L, 1000L / Math.max(1, cps));
    }

    private boolean isValidTarget(LivingEntity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || target == player || !target.isAlive() || target.isDead()) return false;
        if (player.squaredDistanceTo(target) > (double) range * range) return false;
        if (target instanceof PlayerEntity) return players;
        if (target instanceof AnimalEntity) return animals;
        return mobs;
    }

    public boolean players() { return players; }
    public boolean mobs() { return mobs; }
    public boolean animals() { return animals; }
    public int range() { return range; }
    public int cps() { return cps; }

    public void setPlayers(boolean value) { players = value; ClientConfig.setKillAuraPlayers(value); }
    public void setMobs(boolean value) { mobs = value; ClientConfig.setKillAuraMobs(value); }
    public void setAnimals(boolean value) { animals = value; ClientConfig.setKillAuraAnimals(value); }
    public void setRange(int value) { range = Math.max(3, Math.min(6, value)); ClientConfig.setKillAuraRange(range); }
    public void setCps(int value) { cps = Math.max(1, Math.min(20, value)); ClientConfig.setKillAuraCps(cps); }
}
