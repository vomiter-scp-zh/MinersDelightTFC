package com.vomiter.minersdelighttfc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.vomiter.minersdelighttfc.compat.CopperPotFluidIO;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.CookingPotFluidRecipeWrapper;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import net.dries007.tfc.common.items.FluidContainerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = CopperPotBlockEntity.class, remap = false)
public abstract class CopperPotBlockEntityFluidMixin extends BlockEntity implements ICookingPotFluidAccess {
    @Shadow private boolean checkNewRecipe;

    @Unique private final List<Player> minersdelighttfc$players = new ArrayList<>();
    @Unique private final FluidTank minersdelighttfc$tank = new FluidTank(4000) {
        @Override protected void onContentsChanged() { minersdelighttfc$setChangedAndSync(); }
    };
    @Unique private final LazyOptional<IFluidHandler> minersdelighttfc$fluidCap =
            LazyOptional.of(() -> minersdelighttfc$tank);
    @Unique private final ItemStackHandler minersdelighttfc$auxInv = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) { minersdelighttfc$setChangedAndSync(); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && (stack.getItem() instanceof BucketItem
                    || stack.getItem() instanceof FluidContainerItem
                    || FluidUtil.getFluidHandler(stack).isPresent());
        }
        @Override public int getSlotLimit(int slot) { return 1; }
    };

    protected CopperPotBlockEntityFluidMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public FluidTank sdtfc$getTank() { return minersdelighttfc$tank; }
    @Override public ItemStackHandler sdtfc$getAuxInv() { return minersdelighttfc$auxInv; }
    @Override public ItemStackHandler sdtfc$getInventory() {
        return ((CopperPotBlockEntity) (Object) this).getInventory();
    }
    @Override public void sdtfc$updateFluidIOSlots() {
        CopperPotFluidIO.updateFluidIOSlots((CopperPotBlockEntity) (Object) this);
    }
    @Override public void sdtfc$addPlayer(Player player) {
        if (!minersdelighttfc$players.contains(player)) minersdelighttfc$players.add(player);
    }
    @Override public void sdtfc$removePlayer(Player player) { minersdelighttfc$players.remove(player); }
    @Override public List<Player> sdtfc$getPlayers() { return minersdelighttfc$players; }
    @Override public void sdtfc$setCheckNewRecipe(boolean value) { checkNewRecipe = value; }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private <T> void minersdelighttfc$fluidCapability(
            Capability<T> capability, @Nullable Direction side,
            CallbackInfoReturnable<LazyOptional<T>> cir
    ) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            cir.setReturnValue(minersdelighttfc$fluidCap.cast());
        }
    }

    @Inject(method = "cookingTick", at = @At("HEAD"))
    private static void minersdelighttfc$fluidIO(
            Level level, BlockPos pos, BlockState state, CopperPotBlockEntity pot, CallbackInfo ci
    ) {
        if (!level.isClientSide) CopperPotFluidIO.updateFluidIOSlots(pot);
    }

    @WrapOperation(
            method = "cookingTick",
            at = @At(value = "INVOKE", target = "Lcom/sammy/minersdelight/content/block/copper_pot/CopperPotBlockEntity;createFakeRecipeWrapper()Lnet/minecraftforge/items/wrapper/RecipeWrapper;")
    )
    private static RecipeWrapper minersdelighttfc$fluidRecipeWrapper(
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

    @Inject(method = "processCooking", at = @At("RETURN"))
    private void minersdelighttfc$drainFluid(
            CookingPotRecipe recipe, CopperPotBlockEntity self, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue() || !(recipe instanceof IFluidRequiringRecipe fluidRecipe)) return;
        if (fluidRecipe.sdtfc$getFluidIngredient() != null && fluidRecipe.sdtfc$getRequiredFluidAmount() > 0) {
            minersdelighttfc$tank.drain(
                    fluidRecipe.sdtfc$getRequiredFluidAmount(), IFluidHandler.FluidAction.EXECUTE
            );
        }
    }

    @Inject(method = "load", at = @At("TAIL"), remap = true)
    private void minersdelighttfc$loadFluid(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("minersdelighttfc:pot_tank", Tag.TAG_COMPOUND)) {
            minersdelighttfc$tank.readFromNBT(tag.getCompound("minersdelighttfc:pot_tank"));
        }
        if (tag.contains("minersdelighttfc:aux_inv", Tag.TAG_COMPOUND)) {
            minersdelighttfc$auxInv.deserializeNBT(tag.getCompound("minersdelighttfc:aux_inv"));
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
    private void minersdelighttfc$saveFluid(CompoundTag tag, CallbackInfo ci) {
        minersdelighttfc$writeFluid(tag);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"), cancellable = true, remap = true)
    private void minersdelighttfc$updateTag(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        minersdelighttfc$writeFluid(tag);
        cir.setReturnValue(tag);
    }

    @Inject(method = "setRemoved", at = @At("TAIL"), remap = true)
    private void minersdelighttfc$invalidateFluid(CallbackInfo ci) {
        minersdelighttfc$fluidCap.invalidate();
    }

    @Unique private void minersdelighttfc$writeFluid(CompoundTag tag) {
        CompoundTag tankTag = new CompoundTag();
        minersdelighttfc$tank.writeToNBT(tankTag);
        tag.put("minersdelighttfc:pot_tank", tankTag);
        tag.put("minersdelighttfc:aux_inv", minersdelighttfc$auxInv.serializeNBT());
    }

    @Unique private void minersdelighttfc$setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
