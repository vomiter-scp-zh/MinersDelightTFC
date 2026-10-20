package com.vomiter.minersdelighttfc.menu;

import com.mojang.datafixers.util.Pair;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotResultSlot;
import com.sammy.minersdelight.setup.MDBlocks;
import com.vomiter.minersdelighttfc.mixin.CopperPotBlockEntityAccessor;
import com.vomiter.minersdelighttfc.registry.MDTFCMenuTypes;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMealSlot;

public final class CopperPotFluidMenu extends AbstractContainerMenu {
    private static final int PLAYER_START = 5;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final BlockPos pos;
    private final CopperPotBlockEntity pot;
    private final ICookingPotFluidAccess fluidAccess;
    private final ContainerData cookingData;

    public CopperPotFluidMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public CopperPotFluidMenu(int id, Inventory playerInventory, BlockPos pos) {
        super(MDTFCMenuTypes.COPPER_POT_FLUID.get(), id);
        this.pos = pos;
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        this.pot = blockEntity instanceof CopperPotBlockEntity copperPot ? copperPot : null;
        this.fluidAccess = pot instanceof ICookingPotFluidAccess access ? access : null;
        this.cookingData = pot == null ? new SimpleContainerData(2)
                : ((CopperPotBlockEntityAccessor) pot).mdtfc$getCookingPotData();

        if (fluidAccess == null || pot == null) {
            SimpleContainer dummy = new SimpleContainer(1);
            addSlot(new Slot(dummy, 0, 57, 21));
            addSlot(new Slot(dummy, 0, 57, 55));
            addSlot(new Slot(dummy, 0, 124, 26));
            addSlot(new Slot(dummy, 0, 92, 55));
            addSlot(new Slot(dummy, 0, 124, 55));
        } else {
            ItemStackHandler aux = fluidAccess.sdtfc$getAuxInv();
            addSlot(new FluidInputSlot(aux, 0, 57, 21));
            addSlot(new FluidOutputSlot(aux, 1, 57, 55));
            addSlot(new CookingPotMealSlot(pot.getInventory(), CopperPotBlockEntity.MEAL_DISPLAY_SLOT, 124, 26));
            addSlot(new SlotItemHandler(pot.getInventory(), CopperPotBlockEntity.CONTAINER_SLOT, 92, 55) {
                @Override public Pair<net.minecraft.resources.ResourceLocation, net.minecraft.resources.ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, CopperPotMenu.EMPTY_CONTAINER_SLOT_CUP);
                }
            });
            addSlot(new CopperPotResultSlot(
                    playerInventory.player, pot, pot.getInventory(), CopperPotBlockEntity.OUTPUT_SLOT, 124, 55
            ));
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        addDataSlots(cookingData);
    }

    public BlockPos getPos() { return pos; }
    public CopperPotBlockEntity getPot() { return pot; }
    public FluidStack getFluid() {
        return fluidAccess == null ? FluidStack.EMPTY : fluidAccess.sdtfc$getTank().getFluid();
    }
    public int getFluidCapacity() {
        return fluidAccess == null ? 4000 : fluidAccess.sdtfc$getTank().getCapacity();
    }
    public int getCookProgressionScaled() {
        int progress = cookingData.get(0);
        int total = cookingData.get(1);
        return total == 0 || progress == 0 ? 0 : progress * 24 / total;
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return pot != null && stillValid(
                net.minecraft.world.inventory.ContainerLevelAccess.create(pot.getLevel(), pos),
                player, MDBlocks.COPPER_POT.get()
        );
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(source, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(source, 0, 1, false)) {
            int hotbarStart = PLAYER_START + 27;
            if (index < hotbarStart) {
                if (!moveItemStackTo(source, hotbarStart, PLAYER_END, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(source, PLAYER_START, hotbarStart, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    private static final class FluidInputSlot extends SlotItemHandler {
        private FluidInputSlot(ItemStackHandler handler, int index, int x, int y) { super(handler, index, x, y); }
        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class FluidOutputSlot extends SlotItemHandler {
        private FluidOutputSlot(ItemStackHandler handler, int index, int x, int y) { super(handler, index, x, y); }
        @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        @Override public int getMaxStackSize() { return 1; }
    }
}
