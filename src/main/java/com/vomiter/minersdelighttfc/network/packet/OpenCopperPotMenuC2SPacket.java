package com.vomiter.minersdelighttfc.network.packet;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.SDCookingPotFluidMenu;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

public record OpenCopperPotMenuC2SPacket() {
    public static void encode(OpenCopperPotMenuC2SPacket pkt, FriendlyByteBuf buf) {
    }

    public static OpenCopperPotMenuC2SPacket decode(FriendlyByteBuf buf) {
        return new OpenCopperPotMenuC2SPacket();
    }

    public static void handle(OpenCopperPotMenuC2SPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = (NetworkEvent.Context)ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp != null) {
                AbstractContainerMenu patt1074$temp = sp.containerMenu;
                if (patt1074$temp instanceof SDCookingPotFluidMenu) {
                    SDCookingPotFluidMenu m = (SDCookingPotFluidMenu)patt1074$temp;
                    BlockPos pos = m.getPos();
                    BlockEntity be = sp.level().getBlockEntity(pos);
                    if (be instanceof CookingPotBlockEntity) {
                        CookingPotBlockEntity pot = (CookingPotBlockEntity)be;
                        NetworkHooks.openScreen(sp, pot, pos);
                    }
                }

            }
        });
        c.setPacketHandled(true);
    }
}
