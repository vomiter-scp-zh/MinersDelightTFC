package com.vomiter.minersdelighttfc.block;

import com.vomiter.minersdelighttfc.registry.MDTFCBlocks;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.blocks.crop.DefaultCropBlock;
import net.dries007.tfc.common.blocks.crop.CropHelpers;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class CaveCarrotCropBlock extends DefaultCropBlock {
    public static final IntegerProperty AGE = TFCBlockStateProperties.getAgeProperty(4);

    public CaveCarrotCropBlock(ExtendedProperties properties) {
        super(properties, 4,
                MDTFCBlocks.DEAD_CAVE_CARROT,
                MDTFCItems.CAVE_CARROT_SEEDS,
                Crop.CARROT.getPrimaryNutrient(),
                Crop.CARROT.getClimateRange()
        );
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public float getGrowthLimit(Level level, BlockPos pos, BlockState state) {
        // Keep TFC's growth/yield/expiry mechanics, but allow underground farming.
        return CropHelpers.GROWTH_LIMIT;
    }
}
