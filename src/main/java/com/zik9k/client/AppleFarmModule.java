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
import java.util.Queue;

/** Automatic oak-tree farming state machine. */
public final class AppleFarmModule extends Module {
    private enum State { IDLE, HARVEST, CLEAR_LEAVES, COLLECT_SAPLING, RETURN_TO_CENTER, PLANT, GROW, WAIT_GROWTH }

    private State state = State.IDLE;
    private BlockPos farmCenter;
    private BlockPos targetTreeBase;
    private long nextAction;
    private final Queue<BlockPos> leafQueue = new ArrayDeque<>();

    public AppleFarmModule() {
        super("Apple Farm", "Automatically farms oak trees from a fixed farm point", ModuleCategory.PLAYER);
    }

    @Override
    protected boolean canEnable() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && hasRequiredItems(player);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        farmCenter = player.getBlockPos();
        targetTreeBase = findOakBase(client, farmCenter);
        if (targetTreeBase == null) targetTreeBase = findPlantSpot(client, farmCenter);
        if (targetTreeBase == null) {
            setEnabled(false);
            return;
        }
        state = client.world.getBlockState(targetTreeBase).isOf(Blocks.OAK_LOG) ? State.HARVEST : State.PLANT;
        nextAction = 0L;
    }

    @Override
    protected void onDisable() {
        state = State.IDLE;
        farmCenter = null;
        targetTreeBase = null;
        leafQueue.clear();
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null || client.currentScreen != null) return;
        if (!hasRequiredItems(player)) {
            setEnabled(false);
            return;
        }
        if (System.currentTimeMillis() < nextAction) return;

        switch (state) {
            case HARVEST -> harvestTree(client, player);
            case CLEAR_LEAVES -> clearLeaves(client, player);
            case COLLECT_SAPLING -> collectSapling(client, player);
            case RETURN_TO_CENTER -> moveTo(player, farmCenter, State.PLANT);
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
            if (targetTreeBase == null) { state = State.COLLECT_SAPLING; return; }
        }
        BlockPos log = findNearestLog(client, targetTreeBase);
        if (log == null) { state = State.CLEAR_LEAVES; return; }
        if (!moveTo(player, log, State.HARVEST)) return;
        breakBlock(client, player, log, AxeItem.class);
    }

    private void clearLeaves(MinecraftClient client, ClientPlayerEntity player) {
        if (leafQueue.isEmpty()) {
            BlockPos base = targetTreeBase != null ? targetTreeBase : farmCenter;
            if (base == null) { state = State.COLLECT_SAPLING; return; }
            for (int x = -3; x <= 3; x++) {
                for (int y = 0; y <= 7; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos p = base.add(x, y, z);
                        if (client.world.getBlockState(p).isOf(Blocks.OAK_LEAVES)) leafQueue.add(p);
                    }
                }
            }
        }
        BlockPos leaf = leafQueue.peek();
        if (leaf == null) { state = State.COLLECT_SAPLING; return; }
        if (!client.world.getBlockState(leaf).isOf(Blocks.OAK_LEAVES)) { leafQueue.poll(); return; }
        if (!moveTo(player, leaf, State.CLEAR_LEAVES)) return;
        breakBlock(client, player, leaf, HoeItem.class);
        if (client.world.getBlockState(leaf).isAir()) leafQueue.poll();
    }

    private void collectSapling(MinecraftClient client, ClientPlayerEntity player) {
        ItemEntity closest = null;
        double best = 36.0D;
        for (ItemEntity item : client.world.getEntitiesByClass(ItemEntity.class,
                player.getBoundingBox().expand(6.0D), entity -> entity.getStack().isOf(Items.OAK_SAPLING))) {
            double distance = player.squaredDistanceTo(item);
            if (distance < best) { best = distance; closest = item; }
        }
        if (closest == null) { state = State.RETURN_TO_CENTER; return; }
        moveTo(player, closest.getBlockPos(), State.COLLECT_SAPLING);
    }

    private void plant(MinecraftClient client, ClientPlayerEntity player) {
        if (targetTreeBase == null) { setEnabled(false); return; }
        BlockPos spot = targetTreeBase;
        if (!client.world.getBlockState(spot).isAir()) { setEnabled(false); return; }
        if (!moveTo(player, spot, State.PLANT)) return;
        int saplingSlot = findItemSlot(player, Items.OAK_SAPLING);
        if (saplingSlot < 0) { setEnabled(false); return; }
        selectHotbarSlot(player, saplingSlot);
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND,
                new BlockHitResult(Vec3d.ofCenter(spot), Direction.UP, spot.down(), false));
        state = client.world.getBlockState(spot).isOf(Blocks.OAK_SAPLING) ? State.GROW : State.PLANT;
    }

    private void grow(MinecraftClient client, ClientPlayerEntity player) {
        if (targetTreeBase == null) { setEnabled(false); return; }
        if (!moveTo(player, targetTreeBase, State.GROW)) return;
        int boneSlot = findItemSlot(player, Items.BONE_MEAL);
        if (boneSlot < 0) { setEnabled(false); return; }
        selectHotbarSlot(player, boneSlot);
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND,
                new BlockHitResult(Vec3d.ofCenter(targetTreeBase), Direction.UP, targetTreeBase, false));
        state = State.WAIT_GROWTH;
    }

    private void awaitGrowth(MinecraftClient client) {
        if (targetTreeBase == null || client.world == null) { setEnabled(false); return; }
        Block block = client.world.getBlockState(targetTreeBase).getBlock();
        if (block == Blocks.OAK_LOG || block == Blocks.OAK_WOOD) state = State.HARVEST;
        else if (block != Blocks.OAK_SAPLING) state = State.PLANT;
    }

    private void breakBlock(MinecraftClient client, ClientPlayerEntity player, BlockPos pos, Class<?> toolType) {
        int slot = findToolSlot(player, toolType);
        if (slot < 0) { setEnabled(false); return; }
        selectHotbarSlot(player, slot);
        player.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(pos));
        client.interactionManager.attackBlock(pos, Direction.UP);
        client.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
        player.swingHand(Hand.MAIN_HAND);
    }

    private boolean moveTo(ClientPlayerEntity player, BlockPos target, State nextState) {
        if (target == null) { setEnabled(false); return false; }
        Vec3d destination = Vec3d.ofCenter(target);
        Vec3d delta = destination.subtract(player.getPos());
        if (delta.horizontalLengthSquared() < 4.0D && Math.abs(delta.y) < 2.5D) { state = nextState; return true; }
        Vec3d horizontal = new Vec3d(delta.x, 0.0D, delta.z);
        if (horizontal.lengthSquared() > 0.01D) horizontal = horizontal.normalize().multiply(0.16D);
        player.setVelocity(horizontal.x, player.getVelocity().y, horizontal.z);
        return false;
    }

    private boolean hasRequiredItems(ClientPlayerEntity player) {
        return findItemSlot(player, Items.BONE_MEAL) >= 0
                && findToolSlot(player, AxeItem.class) >= 0
                && findToolSlot(player, HoeItem.class) >= 0
                && findItemSlot(player, Items.OAK_SAPLING) >= 0;
    }

    private int findToolSlot(ClientPlayerEntity player, Class<?> toolType) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && toolType.isInstance(stack.getItem())) return i;
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
        if (inventorySlot >= 0 && inventorySlot < 9) {
            player.getInventory().setSelectedSlot(inventorySlot);
            return;
        }
        if (inventorySlot >= 9) {
            ItemStack wanted = player.getInventory().getStack(inventorySlot);
            for (int i = 0; i < 9; i++) {
                if (ItemStack.areItemsEqual(player.getInventory().getStack(i), wanted)) {
                    player.getInventory().setSelectedSlot(i);
                    return;
                }
            }
        }
    }

    private BlockPos findOakBase(MinecraftClient client, BlockPos center) {
        if (center == null || client.world == null) return null;
        for (int r = 0; r <= 4; r++) for (int y = 0; y <= 3; y++) for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            BlockPos p = center.add(x, y, z);
            if (client.world.getBlockState(p).isOf(Blocks.OAK_LOG)) return p;
        }
        return null;
    }

    private BlockPos findNearestLog(MinecraftClient client, BlockPos base) {
        BlockPos best = null; double bestDist = Double.MAX_VALUE;
        for (int x = -2; x <= 2; x++) for (int y = 0; y <= 8; y++) for (int z = -2; z <= 2; z++) {
            BlockPos p = base.add(x, y, z);
            if (!client.world.getBlockState(p).isOf(Blocks.OAK_LOG)) continue;
            double d = p.getSquaredDistance(base);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }

    private BlockPos findPlantSpot(MinecraftClient client, BlockPos center) {
        if (center == null || client.world == null) return null;
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos p = center.add(x, 0, z), soil = p.down();
            if (client.world.getBlockState(p).isAir()
                    && (client.world.getBlockState(soil).isOf(Blocks.DIRT) || client.world.getBlockState(soil).isOf(Blocks.GRASS_BLOCK))) return p;
        }
        return null;
    }
}
