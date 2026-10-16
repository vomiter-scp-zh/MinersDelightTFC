package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic.CookingPotCookingHandler;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.common.food.FoodContainerExpansion;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.FoodItemContainerApply;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.HashMap;
import java.util.Objects;

@Mixin(value = CopperPotBlockEntity.class, remap = false)
public abstract class CopperPotBlockEntityMixin extends BlockEntity {
    @Shadow @Final private ItemStackHandler inventory;

    public CopperPotBlockEntityMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    @Shadow public abstract ItemStack getMeal();
    @Shadow public abstract boolean isContainerValid(ItemStack containerItem);
    @Unique private ItemStack minersdelighttfc$latestResult = ItemStack.EMPTY;

    @WrapOperation(
            method = {"canCook", "processCooking"},
            at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", remap = true)
    )
    private ItemStack minersdelighttfc$dynamicResult(
            CookingPotRecipe recipe, RegistryAccess access, Operation<ItemStack> original
    ) {
        ItemStack result = original.call(recipe, access);
        Level level = getLevel();
        ICookingPotFluidAccess fluidAccess = (ICookingPotFluidAccess) this;
        ItemStack adapted = level == null ? result.copy() : CookingPotCookingHandler.calculateDynamicOutput(
                inventory,
                Objects.requireNonNull(fluidAccess.sd$getFluidHandler()).getFluidInTank(0),
                level,
                recipe,
                4
                );
        minersdelighttfc$latestResult = adapted.copy();
        return adapted;
    }

    @WrapOperation(
            method = {"canCook", "processCooking"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private boolean minersdelighttfc$foodAwareComparison(
            ItemStack stored, ItemStack incoming, Operation<Boolean> original
    ) {
        if (FoodCapability.get(stored) != null || FoodCapability.get(incoming) != null) {
            return FoodCapability.areStacksStackableExceptCreationDate(stored, incoming);
        }
        return original.call(stored, incoming);
    }

    @WrapOperation(
            method = "processCooking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V", remap = true)
    )
    private void minersdelighttfc$foodAwareMerge(
            ItemStack stored, int amount, Operation<Void> original, @Local(argsOnly = true) CookingPotRecipe recipe
    ) {
        if (this.minersdelighttfc$latestResult.isEmpty()) {
            ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());
            FoodCapability.mergeItemStacks(stored, resultStack.copy());
        } else {
            FoodCapability.mergeItemStacks(stored, this.minersdelighttfc$latestResult);
        }
    }

    @WrapOperation(
            method = "isContainerValid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private boolean minersdelighttfc$expandContainer(
            ItemStack expected, ItemStack supplied, Operation<Boolean> original
    ) {
        return original.call(expected, supplied)
                || FoodContainerExpansion.isExtraValid(expected.getItem(), supplied);
    }

    @Inject(method = "useHeldItemOnMeal", at = @At("HEAD"), cancellable = true)
    private void minersdelighttfc$applyHeldContainer(
            ItemStack container, CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack meal = getMeal();
        if (!isContainerValid(container) || meal.isEmpty()) return;

        IFood containerFood = FoodCapability.get(container);
        if (containerFood != null && containerFood.isRotten()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack actualContainer = container.copyWithCount(1);
        container.shrink(1);
        ItemStack serving = meal.split(1);
        cir.setReturnValue(applyContainer(serving, actualContainer));
    }

    @Inject(method = "useStoredContainersOnMeal", at = @At("HEAD"), cancellable = true)
    private void minersdelighttfc$applyStoredContainers(CallbackInfo ci) {
        ItemStack meal = inventory.getStackInSlot(CopperPotBlockEntity.MEAL_DISPLAY_SLOT);
        ItemStack containers = inventory.getStackInSlot(CopperPotBlockEntity.CONTAINER_SLOT);
        ItemStack output = inventory.getStackInSlot(CopperPotBlockEntity.OUTPUT_SLOT);
        if (!isContainerValid(containers) || meal.isEmpty()) return;

        IFood containerFood = FoodCapability.get(containers);
        if (containerFood != null && containerFood.isRotten()) {
            ci.cancel();
            return;
        }

        int capacity = output.isEmpty() ? meal.getMaxStackSize()
                : output.getMaxStackSize() - output.getCount();
        int mealCount = Math.min(Math.min(meal.getCount(), containers.getCount()), capacity);
        if (mealCount <= 0) {
            ci.cancel();
            return;
        }

        ItemStack serving = meal.copyWithCount(mealCount);
        applyContainer(serving, containers.copyWithCount(1));
        if (output.isEmpty()) {
            meal.shrink(mealCount);
            containers.shrink(mealCount);
            inventory.setStackInSlot(CopperPotBlockEntity.OUTPUT_SLOT, serving);
        } else if (FoodCapability.areStacksStackableExceptCreationDate(output, serving)) {
            meal.shrink(mealCount);
            containers.shrink(mealCount);
            FoodCapability.mergeItemStacks(output, serving);
        }
        ci.cancel();
    }

    @Unique
    private static ItemStack applyContainer(ItemStack serving, ItemStack container) {
        return serving.is(SDTags.ItemTags.SOUPS)
                ? FoodItemContainerApply.applySoup(serving, container)
                : FoodItemContainerApply.applyGeneral(serving, container);
    }

    @WrapOperation(method = {"canCook", "processCooking"}, at = @At(value = "INVOKE", target = "Ljava/util/HashMap;containsKey(Ljava/lang/Object;)Z"))
    private boolean mdfc$disableConversion(HashMap instance, Object key, Operation<Boolean> original){
        return false;
    }
}
