package com.vomiter.minersdelighttfc.network.packet;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.menu.CopperPotFluidMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCopperPotFluidMenuC2SPacket(int containerId, BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenCopperPotFluidMenuC2SPacket> TYPE = new Type<>(MinersDelightTFC.id("open_fluid_menu"));
    public static final StreamCodec<FriendlyByteBuf, OpenCopperPotFluidMenuC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenCopperPotFluidMenuC2SPacket::containerId,
            BlockPos.STREAM_CODEC, OpenCopperPotFluidMenuC2SPacket::pos, OpenCopperPotFluidMenuC2SPacket::new);

    @Override public Type<OpenCopperPotFluidMenuC2SPacket> type() { return TYPE; }

    public static void handle(OpenCopperPotFluidMenuC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || player.containerMenu.containerId != packet.containerId()
                    || !(player.containerMenu instanceof CopperPotMenu menu)
                    || !menu.blockEntity.getBlockPos().equals(packet.pos())
                    || !menu.stillValid(player)
                    || player.distanceToSqr(packet.pos().getCenter()) > 64
                    || !player.serverLevel().hasChunkAt(packet.pos())
                    || !(player.level().getBlockEntity(packet.pos()) instanceof CopperPotBlockEntity pot)) return;
            player.connection.send(ClientboundBlockEntityDataPacket.create(pot));
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, ignored) -> new CopperPotFluidMenu(id, inventory, packet.pos()),
                    Component.translatable("gui.minersdelighttfc.copper_pot_fluid")), packet.pos());
        });
    }
}
