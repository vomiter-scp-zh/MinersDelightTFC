package com.vomiter.minersdelighttfc.client;

import com.vomiter.minersdelighttfc.client.screen.CopperPotFluidScreen;
import com.vomiter.minersdelighttfc.registry.MDTFCMenuTypes;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.minersdelighttfc.registry.MDTFCBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.dries007.tfc.client.model.ContainedFluidModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

public final class ClientSetup {
    private ClientSetup() {}

    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(MDTFCBlocks.CAVE_CARROT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MDTFCBlocks.DEAD_CAVE_CARROT.get(), RenderType.cutout());
        });
        event.enqueueWork(() -> MenuScreens.register(
                MDTFCMenuTypes.COPPER_POT_FLUID.get(), CopperPotFluidScreen::new
        ));
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new ContainedFluidModel.Colors(), MDTFCItems.COPPER_CUP.get());
    }
}
