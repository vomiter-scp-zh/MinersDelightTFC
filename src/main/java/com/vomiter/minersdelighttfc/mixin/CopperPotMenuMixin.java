package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.CookingPotFluidRecipeWrapper;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
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

    @WrapOperation(
            method = "recipeMatches",
            at = @At(value = "INVOKE", target = "Lcom/sammy/minersdelight/content/block/copper_pot/CopperPotBlockEntity;createFakeRecipeWrapper()Lnet/minecraftforge/items/wrapper/RecipeWrapper;")
    )
    private RecipeWrapper mdtfc$fluidRecipeBookWrapper(
            CopperPotBlockEntity pot, Operation<RecipeWrapper> original
    ) {
        ItemStackHandler source = pot.getInventory();
        ItemStackHandler mapped = new ItemStackHandler(9);
        for (int index = 0; index < CopperPotBlockEntity.INVENTORY_SIZE; index++) {
            mapped.setStackInSlot(index < 4 ? index : index + 2, source.getStackInSlot(index));
        }
        return new CookingPotFluidRecipeWrapper(
                mapped, ((ICookingPotFluidAccess) pot).sdtfc$getTank().getFluid()
        );
    }
}
