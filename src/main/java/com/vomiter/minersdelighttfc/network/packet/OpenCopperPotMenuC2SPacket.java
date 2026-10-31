package com.vomiter.minersdelighttfc.network.packet;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.menu.CopperPotFluidMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCopperPotMenuC2SPacket() implements CustomPacketPayload {
    public static final Type<OpenCopperPotMenuC2SPacket> TYPE = new Type<>(MinersDelightTFC.id("open_cooking_menu"));
    public static final StreamCodec<FriendlyByteBuf, OpenCopperPotMenuC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenCopperPotMenuC2SPacket());

    @Override public Type<OpenCopperPotMenuC2SPacket> type() { return TYPE; }

    public static void handle(OpenCopperPotMenuC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof CopperPotFluidMenu menu && menu.stillValid(player)) {
                player.openMenu(menu.getPot(), menu.getPos());
            }
        });
    }
}
