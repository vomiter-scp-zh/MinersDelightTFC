package com.vomiter.minersdelighttfc.network.packet;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.vomiter.minersdelighttfc.menu.CopperPotFluidMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public record OpenCopperPotFluidMenuC2SPacket(int containerId, BlockPos pos) {
    public static void encode(OpenCopperPotFluidMenuC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.containerId());
        buffer.writeBlockPos(packet.pos());
    }
    public static OpenCopperPotFluidMenuC2SPacket decode(FriendlyByteBuf buffer) {
        return new OpenCopperPotFluidMenuC2SPacket(buffer.readVarInt(), buffer.readBlockPos());
    }
    public static void handle(OpenCopperPotFluidMenuC2SPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.containerMenu.containerId != packet.containerId()
                    || !(player.containerMenu instanceof CopperPotMenu)) return;
            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());
            if (!(blockEntity instanceof com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity)) return;
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (id, inventory, ignored) -> new CopperPotFluidMenu(id, inventory, packet.pos()),
                    Component.translatable("gui.minersdelighttfc.copper_pot_fluid")
            ), packet.pos());
        });
        ctx.setPacketHandled(true);
    }
}
