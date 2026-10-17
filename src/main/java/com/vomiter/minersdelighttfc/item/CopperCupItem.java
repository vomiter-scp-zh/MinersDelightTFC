package com.vomiter.minersdelighttfc.item;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.items.JugItem;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Drinkable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public final class CopperCupItem extends JugItem {
    public CopperCupItem(Properties properties) {
        super(properties, TFCConfig.SERVER.jugCapacity, TFCTags.Fluids.USABLE_IN_JUG);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, @NotNull LivingEntity entity) {
        // JugItem's implementation can destroy the container. Only perform its drink step.
        if (!level.isClientSide && entity instanceof Player player) {
            stack.getCapability(Capabilities.FLUID_ITEM).ifPresent(handler -> {
                Drinkable drinkable = Drinkable.get(handler.getFluidInTank(0).getFluid());
                if (drinkable != null) {
                    FluidStack drained = handler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                    if (!drained.isEmpty()) drinkable.onDrink(player, drained.getAmount());
                }
            });
        }
        return stack;
    }
}
