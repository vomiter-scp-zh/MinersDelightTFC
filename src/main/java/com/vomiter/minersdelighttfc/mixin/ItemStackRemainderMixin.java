package com.vomiter.minersdelighttfc.mixin;

import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackRemainderMixin implements IItemStackExtension {
    @Override
    public ItemStack getCraftingRemainingItem() {
        // Forge supplies this as an interface default, not a method on ItemStack.
        // Keep its normal dispatch, then replace only the legacy empty copper cup.
        ItemStack stack = (ItemStack) (Object) this;
        return MDTFCItems.replaceCopperCup(stack.getItem().getCraftingRemainingItem(stack));
    }
}
