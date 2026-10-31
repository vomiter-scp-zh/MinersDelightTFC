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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.ItemCapabilities;
import com.sammy.minersdelight.setup.MDBlockEntities;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

@Mod(MinersDelightTFC.MOD_ID)
public final class MinersDelightTFC {
    public static final String MOD_ID = "minersdelighttfc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MinersDelightTFC(IEventBus modBus) {
        modBus.addListener(MDTFCNetwork::register);
        modBus.addListener(this::registerCapabilities);
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
            modBus.addListener(ClientSetup::registerScreens);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
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

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, ItemCapabilities::forBucket, MDTFCItems.COPPER_CUP.get());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MDBlockEntities.COPPER_POT.get(),
                (pot, side) -> ((ICookingPotFluidAccess) pot).sd$getFluidHandler());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
