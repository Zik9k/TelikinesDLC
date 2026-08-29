package com.zik9k.client;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Simple oak-tree farming state machine. It intentionally refuses to start
 * unless all required item types are present in the inventory.
 */
public final class AppleFarmModule extends Module {
    private enum State { IDLE, HARVEST, CLEAR_LEAVES, RETURN_TO_CENTER, PLANT, GROW, WAIT_GROWTH }

    private State state = State.IDLE;
    private BlockPos farmCenter;
    private BlockPos targetTreeBase;
    private long nextAction;
    private int selectedToolSlot = -1;
    private final Queue<BlockPos> workQueue = new ArrayDeque<>();
    private final Set<BlockPos> visited = new HashSet<>();

    public AppleFarmModule() {
        super("Apple Farm", "Automatically farms oak trees from a fixed farm point", ModuleCategory.PLAYER);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || !hasRequiredItems(player)) {
            state = State.IDLE;
            return;
        }
        farmCenter = player.getBlockPos();
        targetTreeBase = findOakBase(client, farmCenter);
        if (targetTreeBase == null) targetTreeBase = findPlantSpot(client, farmCenter);
        state = targetTreeBase == null ? State.IDLE : State.HARVEST;
        nextAction = 0L;
    }

    @Override
    protected void onDisable() {
        state = State.IDLE;
        farmCenter = null;
        targetTreeBase = null;
        workQueue.clear();
        visited.clear();
        selectedToolSlot = -1;
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.currentScreen != null) return;
        if (!hasRequiredItems(player)) {
            setEnabled(false);
            return;
        }
        if (System.currentTimeMillis() < nextAction) return;

        switch (state) {
            case HARVEST -> harvestTree(client, player);
            case CLEAR_LEAVES -> clearLeaves(client, player);
            case RETURN_TO_CENTER -> moveTo(client, player, farmCenter, State.PLANT);
            case PLANT -> plant(client, player);
            case GROW -> grow(client, player);
            case WAIT_GROWTH -> awaitGrowth(client);
            default -> { }
        }
        nextAction = System.currentTimeMillis() + 120L;
    }

    private void harvestTree(MinecraftClient client, ClientPlayerEntity player) {
        if (targetTreeBase == null) {
            targetTreeBase = findOakBase(client, farmCenter);
            if (targetTreeBase == null) {
                state = State.RETURN_TO_CENTER;
                return;
            }
        }
        BlockPos log = findNearestLog(client, targetTreeBase);
        if (log == null) {
            state = State.CLEAR_LEAVES;
            return;
        }
        if (!moveNear(player, log)) return;
        if (!useToolOnBlock(client, player, log, AxeItem.class, true)) return;
    }

    private void clearLeaves(MinecraftClient client, ClientPlayerEntity player) {
        workQueue.clear();
        if (targetTreeBase == null) { state = State.RETURN_TO_CENTER; return; }
        BlockPos base = targetTreeBase;
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 7; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos p = base.add(x, y, z);
                    Block b = client.world.getBlockState(p).getBlock();
                    if (b == Blocks.OAK_LEAVES || b == Blocks.AZALEA_LEAVES || b == Blocks.FLOWERING_AZALEA_LEAVES) {
                        workQueue.add(p);
                    }
                }
            }
        }
        if (workQueue.isEmpty()) {
            state = State.RETURN_TO_CENTER;
            return;
        }
        BlockPos leaf = workQueue.peek();
        if (leaf == null) { state = State.RETURN_TO_CENTER; return; }
        if (!moveNear(player, leaf)) return;
        if (useToolOnBlock(client, player, leaf, HoeItem.class, false)) workQueue.poll();
    }

    private void plant(MinecraftClient client, ClientPlayerEntity player) {
        if (farmCenter == null) { setEnabled(false); return; }
        BlockPos soil = farmCenter.down();
        BlockPos spot = farmCenter;
        if (!client.world.getBlockState(spot).isAir()) {
            BlockPos found = findPlantSpot(client, farmCenter);
            if (found == null) { setEnabled(false); return; }
            spot = found;
            targetTreeBase = spot;
        }
        if (!moveNear(player, spot)) return;
        int saplingSlot = findItemSlot(player, Items.OAK_SAPLING);
        if (saplingSlot < 0) { setEnabled(false); return; }
        selectHotbarSlot(player, saplingSlot);
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(spot), Direction.UP, soil, false));
        state = State.GROW;
    }

    private void grow(MinecraftClient client, ClientPlayerEntity player) {
        BlockPos spot = targetTreeBase != null ? targetTreeBase : farmCenter;
        if (spot == null) { setEnabled(false); return; }
        if (!moveNear(player, spot)) return;
        int boneSlot = findItemSlot(player, Items.BONE_MEAL);
        if (boneSlot < 0) { setEnabled(false); return; }
        selectHotbarSlot(player, boneSlot);
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(spot), Direction.UP, spot, false));
        if (client.world.getBlockState(spot).getBlock() == Blocks.OAK_SAPLING) {
            state = State.WAIT_GROWTH;
        } else {
            targetTreeBase = spot;
            state = State.HARVEST;
        }
    }

    private void awaitGrowth(MinecraftClient client) {
        if (targetTreeBase == null || client.world == null) { setEnabled(false); return; }
        Block block = client.world.getBlockState(targetTreeBase).getBlock();
        if (block == Blocks.OAK_LOG || block == Blocks.OAK_WOOD) {
            state = State.HARVEST;
        } else if (block != Blocks.OAK_SAPLING) {
            state = State.PLANT;
        }
    }

    private boolean useToolOnBlock(MinecraftClient client, ClientPlayerEntity player, BlockPos pos, Class<?> toolClass, boolean keepTool) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit) || !hit.getBlockPos().equals(pos)) {
            player.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(pos));
        }
        int slot = findToolSlot(player, toolClass);
        if (slot < 0) { setEnabled(false); return false; }
        selectHotbarSlot(player, slot);
        boolean started = client.interactionManager.attackBlock(pos, Direction.UP);
        if (started) player.swingHand(Hand.MAIN_HAND);
        if (!client.world.getBlockState(pos).isAir()) return true;
        if (!keepTool) selectedToolSlot = slot;
        return true;
    }

    private boolean moveNear(ClientPlayerEntity player, BlockPos target) {
        Vec3d dst = Vec3d.ofCenter(target);
        Vec3d delta = dst.subtract(player.getPos());
        double distance = delta.horizontalLength();
        if (distance < 2.1 && Math.abs(delta.y) < 2.5) return true;
        double maxStep = 0.16;
        Vec3d horizontal = new Vec3d(delta.x, 0, delta.z);
        if (horizontal.lengthSquared() > 0.01) horizontal = horizontal.normalize().multiply(Math.min(maxStep, distance * 0.10));
        player.setVelocity(horizontal.x, player.getVelocity().y, horizontal.z);
        return false;
    }

    private boolean hasRequiredItems(ClientPlayerEntity player) {
        return findItemSlot(player, Items.BONE_MEAL) >= 0
                && findToolSlot(player, AxeItem.class) >= 0
                && findToolSlot(player, HoeItem.class) >= 0
                && findItemSlot(player, Items.OAK_SAPLING) >= 0;
    }

    private int findToolSlot(ClientPlayerEntity player, Class<?> toolClass) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && toolClass.isInstance(stack.getItem())) return i;
        }
        return -1;
    }

    private int findItemSlot(ClientPlayerEntity player, net.minecraft.item.Item item) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    private void selectHotbarSlot(ClientPlayerEntity player, int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot < 9) player.getInventory().selectedSlot = inventorySlot;
        else if (inventorySlot >= 9) {
            ItemStack wanted = player.getInventory().getStack(inventorySlot);
            for (int i = 0; i < 9; i++) {
                if (player.getInventory().getStack(i).isItemEqual(wanted)) {
                    player.getInventory().selectedSlot = i;
                    return;
                }
            }
        }
    }

    private BlockPos findOakBase(MinecraftClient client, BlockPos center) {
        if (center == null || client.world == null) return null;
        for (int r = 0; r <= 4; r++) {
            for (int y = 0; y <= 3; y++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos p = center.add(x, y, z);
                        if (client.world.getBlockState(p).isOf(Blocks.OAK_LOG)) return p;
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findNearestLog(MinecraftClient client, BlockPos base) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = -2; x <= 2; x++) for (int y = 0; y <= 8; y++) for (int z = -2; z <= 2; z++) {
            BlockPos p = base.add(x, y, z);
            if (!client.world.getBlockState(p).isOf(Blocks.OAK_LOG)) continue;
            double d = p.getSquaredDistance(base);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }

    private BlockPos findPlantSpot(MinecraftClient client, BlockPos center) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos p = center.add(x, 0, z);
            BlockPos soil = p.down();
            if (client.world.getBlockState(p).isAir() && (client.world.getBlockState(soil).isOf(Blocks.DIRT) || client.world.getBlockState(soil).isOf(Blocks.GRASS_BLOCK))) return p;
        }
        return null;
    }
}
