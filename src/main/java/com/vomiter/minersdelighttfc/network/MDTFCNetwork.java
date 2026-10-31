package com.vomiter.minersdelighttfc.network;

import com.vomiter.minersdelighttfc.network.packet.OpenCopperPotFluidMenuC2SPacket;
import com.vomiter.minersdelighttfc.network.packet.OpenCopperPotMenuC2SPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class MDTFCNetwork {
    private MDTFCNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(OpenCopperPotFluidMenuC2SPacket.TYPE,
                OpenCopperPotFluidMenuC2SPacket.STREAM_CODEC, OpenCopperPotFluidMenuC2SPacket::handle);
        registrar.playToServer(OpenCopperPotMenuC2SPacket.TYPE,
                OpenCopperPotMenuC2SPacket.STREAM_CODEC, OpenCopperPotMenuC2SPacket::handle);
    }
}
