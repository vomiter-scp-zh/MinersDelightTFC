package com.vomiter.minersdelighttfc.entity;

import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;
import net.dries007.tfc.common.blocks.LargeVesselBlock;
import net.dries007.tfc.common.container.PestContainer;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

@Mod.EventBusSubscriber(modid = MinersDelightTFC.MOD_ID)
public final class SilverfishInfestation {
    private static final String ORIGIN = MinersDelightTFC.MOD_ID + ":infestation_origin";
    private static final int INTERVAL = 100;
    private static final int RADIUS = 2;

    private SilverfishInfestation() {}

    public static void setOrigin(Silverfish silverfish, BlockPos pos) {
        CompoundTag origin = new CompoundTag();
        origin.putLong("Pos", pos.asLong());
        origin.putString("Dimension", silverfish.level().dimension().location().toString());
        // Forge persists this data with the entity, including across chunk unloads.
        silverfish.getPersistentData().put(ORIGIN, origin);
    }

    @Nullable
    private static BlockPos getOrigin(Silverfish silverfish) {
        CompoundTag origin = silverfish.getPersistentData().getCompound(ORIGIN);
        return origin.contains("Pos", Tag.TAG_LONG)
                && origin.getString("Dimension").equals(silverfish.level().dimension().location().toString())
                ? BlockPos.of(origin.getLong("Pos")) : null;
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Silverfish silverfish)) return;
        if (silverfish.goalSelector.getAvailableGoals().stream()
                .noneMatch(goal -> goal.getGoal() instanceof ReturnToInfestationGoal)) {
            // Melee also has priority 4, but this goal only runs without a target.
            // Returning takes precedence over the priority-5 wandering/stone-merge goal.
            silverfish.goalSelector.addGoal(4, new ReturnToInfestationGoal(silverfish));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Silverfish silverfish)
                || !(silverfish.level() instanceof ServerLevel server)
                || !silverfish.isAlive() || silverfish.isNoAi()
                || silverfish.tickCount <= 0 || silverfish.tickCount % INTERVAL != 0
                || !TFCConfig.SERVER.enableInfestations.get()) return;

        BlockPos center = silverfish.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        int foodSlots = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS),
                center.offset(RADIUS, RADIUS, RADIUS))) {
            double distance = center.distSqr(pos);
            if (distance > RADIUS * RADIUS || distance >= nearestDistance || !server.hasChunkAt(pos)) continue;
            BlockEntity blockEntity = server.getBlockEntity(pos);
            if (!canBeInfested(blockEntity)) continue;
            IItemHandler inventory = getInventory(blockEntity);
            if (inventory == null) continue;
            int food = 0;
            for (int slot = 0; slot < inventory.getSlots() && food < 5; slot++) {
                if (Helpers.isItem(inventory.getStackInSlot(slot), TFCTags.Items.FOODS)) food++;
            }
            if (food == 0) continue;
            nearest = pos.immutable();
            nearestDistance = distance;
            foodSlots = food;
        }
        if (nearest != null) {
            // One ordinary TFC probability check per interval, not one per nearby block.
            Helpers.tickInfestation(server, nearest, foodSlots, null);
        }
    }

    private static boolean canBeInfested(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof PestContainer container) return container.canBeInfested();
        // TFC puts PestContainer on the large vessel's menu instead of its block entity.
        return blockEntity instanceof LargeVesselBlockEntity
                && !blockEntity.getBlockState().getValue(LargeVesselBlock.SEALED);
    }

    private static @NotNull IItemHandler getInventory(BlockEntity blockEntity) {
        IItemHandler inventory = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        return inventory != null ? inventory : blockEntity instanceof Container container ? new InvWrapper(container) : null;
    }

    public static void addEggs(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;
        IItemHandler inventory = getInventory(blockEntity);
        if (inventory == null) return;
        int count = level.random.nextInt(6);
        if (count == 0) return;
        ItemStack eggs = new ItemStack(MDItems.SILVERFISH_EGGS.get(), count);
        ItemStack remaining = ItemHandlerHelper.insertItemStacked(inventory, eggs, false);
        if (remaining.getCount() < count) blockEntity.setChanged();
    }

    private static final class ReturnToInfestationGoal extends Goal {
        private final Silverfish silverfish;
        private int repathTicks;

        private ReturnToInfestationGoal(Silverfish silverfish) {
            this.silverfish = silverfish;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BlockPos origin = getOrigin(silverfish);
            return silverfish.getTarget() == null && origin != null
                    && silverfish.level().hasChunkAt(origin)
                    && silverfish.level().getBlockEntity(origin) != null
                    && silverfish.distanceToSqr(Vec3.atBottomCenterOf(origin)) > 2.25;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            repathTicks = 0;
        }

        @Override
        public void tick() {
            BlockPos origin = getOrigin(silverfish);
            if (origin != null && --repathTicks <= 0) {
                repathTicks = 20;
                silverfish.getNavigation().moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 1.0);
            }
        }

        @Override
        public void stop() {
            silverfish.getNavigation().stop();
        }
    }
}