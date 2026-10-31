package com.vomiter.minersdelighttfc.compat;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CopperPotFluidIO {
    public static void updateFluidIOSlots(CopperPotBlockEntity pot) {
        if (pot.getLevel() == null || pot.getLevel().isClientSide) return;
        ICookingPotFluidAccess access = (ICookingPotFluidAccess) pot;
        ItemStackHandler inventory = access.sdtfc$getAuxInv();
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty() || !inventory.getStackInSlot(1).isEmpty()) return;
        FluidHelpers.transferBetweenBlockEntityAndItem(input, pot, pot.getLevel(), pot.getBlockPos(),
                (remaining, container) -> {
                    inventory.setStackInSlot(0, container.isEmpty() ? ItemStack.EMPTY : remaining);
                    inventory.setStackInSlot(1, container.isEmpty() ? remaining : container);
                });
    }
}
