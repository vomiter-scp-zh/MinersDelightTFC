package com.vomiter.minersdelighttfc.mixin;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import net.minecraft.world.inventory.ContainerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CopperPotBlockEntity.class, remap = false)
public interface CopperPotBlockEntityAccessor {
    @Accessor("cookingPotData")
    ContainerData mdtfc$getCookingPotData();
}
