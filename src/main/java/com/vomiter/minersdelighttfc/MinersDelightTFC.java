package com.vomiter.minersdelighttfc;

import com.mojang.logging.LogUtils;
import com.sammy.minersdelight.setup.MDBlocks;
import com.vomiter.minersdelighttfc.client.ClientSetup;
import com.vomiter.minersdelighttfc.loot.NoSkyLightCondition;
import com.vomiter.minersdelighttfc.loot.PiercingDamageCondition;
import com.vomiter.survivorsdelight.mixin.BlockEntityTypeAccessor;
import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import com.vomiter.minersdelighttfc.data.DataGenerators;
import com.vomiter.minersdelighttfc.network.MDTFCNetwork;
import com.vomiter.minersdelighttfc.registry.MDTFCMenuTypes;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.minersdelighttfc.registry.MDTFCBlocks;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

@Mod(MinersDelightTFC.MOD_ID)
public final class MinersDelightTFC {
    public static final String MOD_ID = "minersdelighttfc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MinersDelightTFC() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MDTFCBlocks.register(modBus);
        MDTFCItems.register(modBus);
        MDTFCMenuTypes.register(modBus);
        modBus.addListener(DataGenerators::gatherData);
        modBus.addListener(this::commonSetup);
        modBus.addListener(NoSkyLightCondition::register);
        modBus.addListener(PiercingDamageCondition::register);
        if (FMLEnvironment.dist.isClient()){
            modBus.addListener(ClientSetup::setup);
            modBus.addListener(ClientSetup::registerItemColors);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        MDTFCNetwork.onCommonSetup(event);
        event.enqueueWork(() -> {
            BlockEntityTypeAccessor cropAccessor = (BlockEntityTypeAccessor) TFCBlockEntities.CROP.get();
            Set<Block> crops = new HashSet<>(cropAccessor.getValidBlocks());
            crops.add(MDTFCBlocks.CAVE_CARROT.get());
            cropAccessor.setValidBlocks(Set.copyOf(crops));

            BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) SDBlockEntityTypes.FEAST_DECAYING.get();
            Set<Block> validBlocks = new HashSet<>(accessor.getValidBlocks());
            validBlocks.add(MDBlocks.STUFFED_SQUID.get());
            validBlocks.add(MDBlocks.FAKE_MEATLOAF.get());
            validBlocks.add(MDBlocks.GLAZED_ARACHNID_LIMBS.get());
            accessor.setValidBlocks(Set.copyOf(validBlocks));
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
