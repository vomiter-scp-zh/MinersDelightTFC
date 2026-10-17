package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotRecipeBookComponent;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CopperPotRecipeBookComponent.class, remap = false)
public abstract class CopperPotRecipeBookComponentMixin {
    @ModifyExpressionValue(
            method = "setupGhostRecipe",
            at = {
                    @At(value = "INVOKE", target = "Lcom/sammy/minersdelight/repack/registrate/util/entry/ItemEntry;asStack()Lnet/minecraft/world/item/ItemStack;", remap = false),
                    @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;getOutputContainer()Lnet/minecraft/world/item/ItemStack;", remap = false)
            },
            remap = true
    )
    private ItemStack minersdelighttfc$displayCopperCup(ItemStack original) {
        return MDTFCItems.replaceCopperCup(original);
    }
}
