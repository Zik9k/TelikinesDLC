package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class TriggerBotModule extends Module {
    private boolean clickMode;
    private boolean critMode;
    private boolean targetMobs;
    private boolean targetAnimals;
    private boolean targetPlayers;
    private int clicksPerSecond;
    private long nextAttackTime;

    public TriggerBotModule() {
        super("Trigger Bot", "Attacks the entity under your crosshair automatically", ModuleCategory.COMBAT);
        loadSettings();
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null || client.currentScreen != null) return;
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;

        Entity entity = hit.getEntity();
        if (!(entity instanceof LivingEntity target) || target == player || target.isDead() || !target.isAlive()) return;
        if (!isSelectedTarget(target)) return;
        if (player.distanceTo(target) > player.getEntityInteractionRange()) return;
        if (player.getAttackCooldownProgress(0.0f) < 1.0f) return;

        long now = System.currentTimeMillis();
        if (now < nextAttackTime) return;

        if (!clickMode && !critMode) return;
        if (critMode && !clickMode && !canCrit(player)) return;

        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        nextAttackTime = now + Math.max(1L, 1000L / Math.max(1, clicksPerSecond));
    }

    private boolean isSelectedTarget(LivingEntity target) {
        if (target instanceof PlayerEntity) return targetPlayers;
        if (target instanceof AnimalEntity) return targetAnimals;
        return targetMobs;
    }

    private boolean canCrit(ClientPlayerEntity player) {
        if (player.isOnGround() || player.isClimbing() || player.isTouchingWater() || player.isSubmergedInWater()) return false;
        if (player.hasVehicle()) return false;
        return player.getVelocity().y < -0.08 && player.fallDistance > 0.0f;
    }

    private void loadSettings() {
        clickMode = ClientConfig.triggerClickMode();
        critMode = ClientConfig.triggerCritMode();
        targetMobs = ClientConfig.triggerMobs();
        targetAnimals = ClientConfig.triggerAnimals();
        targetPlayers = ClientConfig.triggerPlayers();
        clicksPerSecond = ClientConfig.triggerCps();
    }

    public boolean isClickMode() { return clickMode; }
    public void setClickMode(boolean value) { clickMode = value; ClientConfig.setTriggerClickMode(value); }
    public boolean isCritMode() { return critMode; }
    public void setCritMode(boolean value) { critMode = value; ClientConfig.setTriggerCritMode(value); }
    public boolean isTargetMobs() { return targetMobs; }
    public void setTargetMobs(boolean value) { targetMobs = value; ClientConfig.setTriggerMobs(value); }
    public boolean isTargetAnimals() { return targetAnimals; }
    public void setTargetAnimals(boolean value) { targetAnimals = value; ClientConfig.setTriggerAnimals(value); }
    public boolean isTargetPlayers() { return targetPlayers; }
    public void setTargetPlayers(boolean value) { targetPlayers = value; ClientConfig.setTriggerPlayers(value); }
    public int getClicksPerSecond() { return clicksPerSecond; }
    public void setClicksPerSecond(int value) { clicksPerSecond = Math.max(1, Math.min(20, value)); ClientConfig.setTriggerCps(clicksPerSecond); }
}