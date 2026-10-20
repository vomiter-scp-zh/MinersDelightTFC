package com.vomiter.minersdelighttfc.mixin.compat.jei;

import com.sammy.minersdelight.jei.CopperPotCookingRecipeCategory;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.compat.jei.category.BaseRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.List;

@Mixin(value = CopperPotCookingRecipeCategory.class, remap = false)
public abstract class CopperPotCookingRecipeCategoryMixin {
    @ModifyExpressionValue(
            method = "setRecipe*",
            at = {
                    @At(value = "INVOKE", target = "Lcom/sammy/minersdelight/repack/registrate/util/entry/ItemEntry;asStack()Lnet/minecraft/world/item/ItemStack;"),
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
        FluidStackIngredient ingredient = ((IFluidRequiringRecipe) recipe).sdtfc$getFluidIngredient();
        if (ingredient == null) return;
        List<FluidStack> fluids = BaseRecipeCategory.collapse(ingredient);
        if (fluids.isEmpty()) return;
        var slot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 40);
        slot.setFluidRenderer(1, false, 16, 16);
        slot.addIngredients(ForgeTypes.FLUID_STACK, fluids);
        slot.addRichTooltipCallback((view, tooltip) ->
                view.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fluid -> {
                    tooltip.add(Component.translatable("tooltip.survivorsdelight.fluid_required"));
                    tooltip.add(Component.literal(
                            fluid.getDisplayName().getString() + " · " + fluid.getAmount() + " mB"
                    ));
                })
        );
    }
}
