package com.vomiter.minersdelighttfc.network;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.network.packet.OpenCopperPotFluidMenuC2SPacket;
import com.vomiter.minersdelighttfc.network.packet.OpenCopperPotMenuC2SPacket;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MDTFCNetwork {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    private MDTFCNetwork() {}

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MDTFCNetwork::init);
    }

    private static void init() {
        if (CHANNEL != null) return;
        CHANNEL = NetworkRegistry.newSimpleChannel(
                MinersDelightTFC.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
        );
        int id = 0;
        CHANNEL.messageBuilder(OpenCopperPotFluidMenuC2SPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenCopperPotFluidMenuC2SPacket::encode)
                .decoder(OpenCopperPotFluidMenuC2SPacket::decode)
                .consumerMainThread(OpenCopperPotFluidMenuC2SPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenCopperPotMenuC2SPacket.class, id, NetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenCopperPotMenuC2SPacket::encode)
                .decoder(OpenCopperPotMenuC2SPacket::decode)
                .consumerMainThread(OpenCopperPotMenuC2SPacket::handle)
                .add();
    }
}
