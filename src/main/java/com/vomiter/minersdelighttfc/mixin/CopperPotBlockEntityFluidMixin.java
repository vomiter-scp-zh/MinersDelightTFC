package com.vomiter.minersdelighttfc.mixin;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.vomiter.minersdelighttfc.compat.CopperPotFluidIO;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid_handle.CookingPotFluidRecipeWrapper;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import net.dries007.tfc.common.items.FluidContainerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CopperPotBlockEntity.class, remap = false)
public abstract class CopperPotBlockEntityFluidMixin extends BlockEntity implements ICookingPotFluidAccess {
    @Unique private final List<ServerPlayer> mdtfc$players = new ArrayList<>();
    @Unique private final FluidTank mdtfc$tank = new FluidTank(4000) {
        @Override protected void onContentsChanged() { mdtfc$setChangedAndSync(); }
    };
    @Unique private final ItemStackHandler mdtfc$auxInv = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) { mdtfc$setChangedAndSync(); }
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
    @Override public FluidTank sdtfc$getTank() { return mdtfc$tank; }
    @Override public ItemStackHandler sdtfc$getAuxInv() { return mdtfc$auxInv; }
    @Override public void sdtfc$updateFluidIOSlots() {
        CopperPotFluidIO.updateFluidIOSlots((CopperPotBlockEntity) (Object) this);
    }
    @Override public void sdtfc$addPlayer(ServerPlayer player) {
        if (!mdtfc$players.contains(player)) mdtfc$players.add(player);
    }
    @Override public void sdtfc$removePlayer(ServerPlayer player) { mdtfc$players.remove(player); }
    @Override public List<ServerPlayer> sdtfc$getPlayer() { return mdtfc$players; }

    @Inject(method = "cookingTick", at = @At("HEAD"))
    private static void mdtfc$fluidIO(Level level, BlockPos pos, BlockState state, CopperPotBlockEntity pot, CallbackInfo ci) {
        if (!level.isClientSide) CopperPotFluidIO.updateFluidIOSlots(pot);
    }

    @Inject(method = "createFakeRecipeWrapper", at = @At("RETURN"), cancellable = true)
    private void mdtfc$fluidRecipeWrapper(CallbackInfoReturnable<RecipeWrapper> cir) {
        ItemStackHandler source = ((CopperPotBlockEntity) (Object) this).getInventory();
        ItemStackHandler mapped = new ItemStackHandler(9);
        for (int index = 0; index < CopperPotBlockEntity.INVENTORY_SIZE; index++) {
            mapped.setStackInSlot(index < 4 ? index : index + 2, source.getStackInSlot(index));
        }
        cir.setReturnValue(new CookingPotFluidRecipeWrapper(mapped, mdtfc$tank.getFluid()));
    }

    @Inject(method = "processCooking", at = @At("RETURN"))
    private void mdtfc$drainFluid(RecipeHolder<CookingPotRecipe> recipe, CopperPotBlockEntity self,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && recipe.value() instanceof SDCookingPotRecipe fluidRecipe) {
            mdtfc$tank.drain(fluidRecipe.getFluidAmountMb(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Inject(method = "getAsItem", at = @At("RETURN"))
    private void mdtfc$saveItemContents(CallbackInfoReturnable<ItemStack> cir) {
        if (level == null) return;
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(getType()).toString());
        mdtfc$writeFluid(tag, level.registryAccess());
        // Main inventory slots are dropped separately; store only the added fluid I/O slots.
        cir.getReturnValue().set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = true)
    private void mdtfc$loadFluid(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        mdtfc$readFluid(tag, registries);
    }
    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
    private void mdtfc$saveFluid(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        mdtfc$writeFluid(tag, registries);
    }
    @Inject(method = "getUpdateTag", at = @At("RETURN"), remap = true)
    private void mdtfc$updateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        mdtfc$writeFluid(cir.getReturnValue(), registries);
    }
    @Override public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.handleUpdateTag(tag, registries);
        mdtfc$readFluid(tag, registries);
    }
    @Unique private void mdtfc$readFluid(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("minersdelighttfc:pot_tank", Tag.TAG_COMPOUND)) {
            mdtfc$tank.readFromNBT(registries, tag.getCompound("minersdelighttfc:pot_tank"));
        }
        if (tag.contains("minersdelighttfc:aux_inv", Tag.TAG_COMPOUND)) {
            mdtfc$auxInv.deserializeNBT(registries, tag.getCompound("minersdelighttfc:aux_inv"));
        }
    }
    @Unique private void mdtfc$writeFluid(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("minersdelighttfc:pot_tank", mdtfc$tank.writeToNBT(registries, new CompoundTag()));
        tag.put("minersdelighttfc:aux_inv", mdtfc$auxInv.serializeNBT(registries));
    }
    @Unique private void mdtfc$setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
