package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CopperPotMenu.class, remap = false)
public abstract class CopperPotMenuMixin {
    @WrapOperation(
            method = "quickMoveStack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", remap = true),
            remap = true
    )
    private Item mdtfc$recognizeEmptyCopperCup(ItemStack stack, Operation<Item> original) {
        return MDTFCItems.isEmptyCopperCup(stack) ? MDItems.COPPER_CUP.get() : original.call(stack);
    }

}
