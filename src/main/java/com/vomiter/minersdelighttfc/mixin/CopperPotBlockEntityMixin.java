package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic.CookingPotCookingHandler;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.common.food.FoodContainerExpansion;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.FoodDataBuilder;
import com.vomiter.survivorsdelight.util.FoodItemContainerApply;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

import java.util.Objects;

@Mixin(value = CopperPotBlockEntity.class, remap = false)
public abstract class CopperPotBlockEntityMixin extends BlockEntity {
    @Shadow @Final private ItemStackHandler inventory;

    public CopperPotBlockEntityMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    @Shadow public abstract ItemStack getMeal();
    @Shadow public abstract boolean isContainerValid(ItemStack containerItem);

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
        return level == null ? result.copy() : CookingPotCookingHandler.calculateDynamicOutput(
                inventory,
                Objects.requireNonNull(fluidAccess.sd$getFluidHandler()).getFluidInTank(0),
                level,
                recipe,
                4
                );
    }

    @WrapOperation(
            method = {"canCook", "processCooking"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V", remap = true)
    )
    private void minersdelighttfc$preserveCupFood(
            ItemStack cup, CompoundTag tag, Operation<Void> original,
            @Local(ordinal = 0) ItemStack meal
    ) {
        original.call(cup, tag == null ? null : tag.copy());
        IFood mealFood = FoodCapability.get(meal);
        if (mealFood == null || cup.isEmpty()
                || !(FoodCapability.get(cup) instanceof FoodHandler.Dynamic cupFood)) return;

        // The upstream conversion copies only the ordinary tag, not Forge capabilities.
        // Divide each serving's food values by the actual increase in serving count.
        float servingFactor = (float) meal.getCount() / cup.getCount();
        cupFood.setFood(FoodDataBuilder.from(mealFood.getData()).mul(servingFactor).build());
        cupFood.setCreationDate(mealFood.getCreationDate());
        cupFood.getTraits().clear();
        cupFood.getTraits().addAll(mealFood.getTraits());
        if (mealFood instanceof FoodHandler.Dynamic dynamicMeal) {
            cupFood.setIngredients(dynamicMeal.getIngredients().stream().map(ItemStack::copy).toList());
        }
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
            ItemStack stored, int amount, Operation<Void> original, @Local(ordinal = 0) ItemStack result
    ) {
        if (FoodCapability.get(stored) != null || FoodCapability.get(result) != null) {
            FoodCapability.mergeItemStacks(stored, result.copyWithCount(amount));
        } else {
            original.call(stored, amount);
        }
    }

    @WrapOperation(
            method = "isContainerValid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private boolean minersdelighttfc$expandContainer(
            ItemStack expected, ItemStack supplied, Operation<Boolean> original
    ) {
        ItemStack actualExpected = MDTFCItems.replaceCopperCup(expected);
        ItemStack actualSupplied = MDTFCItems.replaceCopperCup(supplied);
        return original.call(actualExpected, actualSupplied)
                || FoodContainerExpansion.isExtraValid(actualExpected.getItem(), actualSupplied);
    }

    @ModifyExpressionValue(
            method = "processCooking",
            at = {
                    @At(value = "INVOKE", target = "Lcom/sammy/minersdelight/repack/registrate/util/entry/ItemEntry;asStack()Lnet/minecraft/world/item/ItemStack;"),
                    @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;getOutputContainer()Lnet/minecraft/world/item/ItemStack;")
            }
    )
    private ItemStack minersdelighttfc$useCopperCup(ItemStack original) {
        return MDTFCItems.replaceCopperCup(original);
    }

    @Inject(method = "getContainer", at = @At("RETURN"), cancellable = true)
    private void minersdelighttfc$displayCopperCup(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(MDTFCItems.replaceCopperCup(cir.getReturnValue()));
    }

    @Inject(method = "isContainerValid", at = @At("HEAD"), cancellable = true)
    private void minersdelighttfc$requireEmptyCup(ItemStack container, CallbackInfoReturnable<Boolean> cir) {
        if (container.is(MDTFCItems.COPPER_CUP.get()) && !MDTFCItems.isEmptyCopperCup(container)) {
            cir.setReturnValue(false);
        }
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
        container = MDTFCItems.replaceCopperCup(container);
        return serving.is(SDTags.ItemTags.SOUPS)
                ? FoodItemContainerApply.applySoup(serving, container)
                : FoodItemContainerApply.applyGeneral(serving, container);
    }
}
