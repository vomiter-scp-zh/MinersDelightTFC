package com.vomiter.minersdelighttfc.compat;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.ICookingPotRecipeBridge;
import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.TFCPotRecipeBridgeFD;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.PotFluidSyncS2CPacket;
import java.util.List;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

public class CopperPotFluidIO {
    public static void updateFluidIOSlots(CopperPotBlockEntity pot) {
        ICookingPotFluidAccess self = (ICookingPotFluidAccess)pot;
        IFluidTank tank = self.sdtfc$getTank();
        ItemStackHandler inventory = self.sdtfc$getAuxInv();
        ItemStack input = self.sdtfc$getAuxInv().getStackInSlot(0);
        if (!input.isEmpty() && self.sdtfc$getAuxInv().getStackInSlot(1).isEmpty()) {
            assert pot.getLevel() != null;

            FluidHelpers.transferBetweenBlockEntityAndItem(input, pot, pot.getLevel(), pot.getBlockPos(), (newOriginalStack, newContainerStack) -> {
                self.sdtfc$setCheckNewRecipe(true);
                if (pot instanceof ICookingPotRecipeBridge bridgePot) {
                    bridgePot.sdtfc$setCachedBridge((TFCPotRecipeBridgeFD)null);
                }

                if (newContainerStack.isEmpty()) {
                    inventory.setStackInSlot(0, ItemStack.EMPTY);
                    inventory.setStackInSlot(1, newOriginalStack);
                } else {
                    inventory.setStackInSlot(0, newOriginalStack);
                    inventory.setStackInSlot(1, newContainerStack);
                }

                if (!pot.getLevel().isClientSide) {
                    List.copyOf(self.sdtfc$getPlayers()).forEach((player) -> {
                        if (player.distanceToSqr(Vec3.atCenterOf(pot.getBlockPos())) >= (double)64.0F) {
                            self.sdtfc$removePlayer(player);
                        } else {
                            SDNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new PotFluidSyncS2CPacket(pot.getBlockPos(), ForgeRegistries.FLUIDS.getKey(tank.getFluid().getFluid()), tank.getFluidAmount()));
                        }

                    });
                }
            });
        }

    }
}
