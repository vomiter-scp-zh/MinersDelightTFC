package com.vomiter.minersdelighttfc.mixin.compat.jei;

import com.sammy.minersdelight.jei.CopperPotCookingRecipeCategory;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.List;
import java.util.Arrays;

@Mixin(value = CopperPotCookingRecipeCategory.class, remap = false)
public abstract class CopperPotCookingRecipeCategoryMixin {
    @ModifyExpressionValue(
            method = "setRecipe*",
            at = {
                    @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;getOutputContainer()Lnet/minecraft/world/item/ItemStack;")
            }
    )
    private ItemStack mdtfc$displayCopperCup(ItemStack original) {
        return MDTFCItems.replaceCopperCup(original);
    }

    @Inject(method = "setRecipe*", at = @At("TAIL"))
    private void mdtfc$fluidIngredient(
            IRecipeLayoutBuilder builder, CookingPotRecipe recipe, IFocusGroup focuses, CallbackInfo ci
    ) {
        if (!(recipe instanceof SDCookingPotRecipe fluidRecipe) || fluidRecipe.getFluid() == null) return;
        List<FluidStack> fluids = Arrays.stream(fluidRecipe.getFluid().getStacks())
                .map(stack -> stack.copyWithAmount(fluidRecipe.getFluidAmountMb())).toList();
        if (fluids.isEmpty()) return;
        var slot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 40);
        slot.setFluidRenderer(1, false, 16, 16);
        slot.addIngredients(NeoForgeTypes.FLUID_STACK, fluids);
        slot.addRichTooltipCallback((view, tooltip) ->
                view.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fluid -> {
                    tooltip.add(Component.translatable("tooltip.survivorsdelight.fluid_required"));
                    tooltip.add(Component.literal(
                            fluid.getHoverName().getString() + " · " + fluid.getAmount() + " mB"
                    ));
                })
        );
    }
}
