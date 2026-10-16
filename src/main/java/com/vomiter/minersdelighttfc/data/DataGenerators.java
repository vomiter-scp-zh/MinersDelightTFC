package com.vomiter.minersdelighttfc.data;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

public final class DataGenerators {
    private DataGenerators() {}

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        if (event.includeServer()) {
            MDTFCCookingRecipeProvider.gatherData(event, MinersDelightTFC.MOD_ID);
        }
    }
}
