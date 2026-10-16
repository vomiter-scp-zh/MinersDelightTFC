package com.vomiter.minersdelighttfc.client;

import com.vomiter.minersdelighttfc.client.screen.CopperPotFluidScreen;
import com.vomiter.minersdelighttfc.registry.MDTFCMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientSetup {
    private ClientSetup() {}

    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                MDTFCMenuTypes.COPPER_POT_FLUID.get(), CopperPotFluidScreen::new
        ));
    }
}
