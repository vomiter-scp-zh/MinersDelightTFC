package com.vomiter.minersdelighttfc.registry;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.block.CaveCarrotCropBlock;
import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MDTFCBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS, MinersDelightTFC.MOD_ID);

    public static final RegistryObject<Block> CAVE_CARROT = BLOCKS.register("crop/cave_carrot",
            () -> new CaveCarrotCropBlock(cropProperties().blockEntity(TFCBlockEntities.CROP)
                    .serverTicks(CropBlockEntity::serverTick)));
    public static final RegistryObject<Block> DEAD_CAVE_CARROT = BLOCKS.register("dead_crop/cave_carrot",
            () -> new DeadCropBlock(cropProperties(), Crop.CARROT));

    private MDTFCBlocks() {}

    private static ExtendedProperties cropProperties() {
        return ExtendedProperties.of(MapColor.PLANT).noCollission().randomTicks()
                .strength(0.4F).sound(SoundType.CROP).flammable(60, 30);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
