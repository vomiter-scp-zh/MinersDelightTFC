package com.vomiter.minersdelighttfc.item;

import net.dries007.tfc.common.TFCTags;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.dries007.tfc.common.items.JugItem;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.data.Drinkable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public final class CopperCupItem extends JugItem {
    public CopperCupItem(Properties properties) {
        super(properties, TFCConfig.SERVER.jugCapacity, TFCTags.Fluids.USABLE_IN_JUG);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, @NotNull LivingEntity entity) {
        // JugItem's implementation can destroy the container. Only perform its drink step.
        if (!level.isClientSide && entity instanceof Player player) {
            var handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler != null) {
                Drinkable drinkable = Drinkable.get(handler.getFluidInTank(0).getFluid());
                if (drinkable != null) {
                    FluidStack drained = handler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                    if (!drained.isEmpty()) drinkable.onDrink(player, drained.getAmount());
                }
            }
        }
        return stack;
    }
}
