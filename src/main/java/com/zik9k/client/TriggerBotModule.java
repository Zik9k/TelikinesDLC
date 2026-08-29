package com.zik9k.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

public final class TriggerBotModule extends Module {
    private boolean clickMode = true;
    private boolean critMode = false;
    private boolean targetMobs = true;
    private boolean targetAnimals;
    private boolean targetPlayers = true;
    private int clicksPerSecond = 8;
    private long nextAttackTime;

    public TriggerBotModule() {
        super("Trigger Bot", "Attacks the entity under your crosshair automatically", ModuleCategory.COMBAT);
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

        boolean clickReady = clickMode;
        boolean critReady = critMode && canCrit(player);
        if (!clickReady && !critReady) return;

        client.interactionManager.attackEntity(player, target);
        player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        nextAttackTime = now + Math.max(1L, 1000L / clicksPerSecond);
    }

    private boolean isSelectedTarget(LivingEntity target) {
        if (target instanceof PlayerEntity) return targetPlayers;
        if (target instanceof HostileEntity) return targetMobs;
        if (target instanceof AnimalEntity) return targetAnimals;
        return false;
    }

    private boolean canCrit(ClientPlayerEntity player) {
        if (player.isOnGround() || player.isClimbing() || player.isTouchingWater() || player.isSubmergedInWater()) return false;
        if (player.hasVehicle()) return false;
        return player.getVelocity().y < -0.08 && player.fallDistance > 0.0f;
    }

    public boolean isClickMode() { return clickMode; }
    public void setClickMode(boolean value) { clickMode = value; ClientConfig.saveTriggerBot(this); }

    public boolean isCritMode() { return critMode; }
    public void setCritMode(boolean value) { critMode = value; ClientConfig.saveTriggerBot(this); }

    public boolean isTargetMobs() { return targetMobs; }
    public void setTargetMobs(boolean value) { targetMobs = value; ClientConfig.saveTriggerBot(this); }

    public boolean isTargetAnimals() { return targetAnimals; }
    public void setTargetAnimals(boolean value) { targetAnimals = value; ClientConfig.saveTriggerBot(this); }

    public boolean isTargetPlayers() { return targetPlayers; }
    public void setTargetPlayers(boolean value) { targetPlayers = value; ClientConfig.saveTriggerBot(this); }

    public int getClicksPerSecond() { return clicksPerSecond; }
    public void setClicksPerSecond(int value) { clicksPerSecond = Math.max(1, Math.min(20, value)); ClientConfig.saveTriggerBot(this); }

    public void loadSettings(boolean click, boolean crit, boolean mobs, boolean animals, boolean players, int cps) {
        clickMode = click;
        critMode = crit;
        targetMobs = mobs;
        targetAnimals = animals;
        targetPlayers = players;
        clicksPerSecond = Math.max(1, Math.min(20, cps));
    }
}
