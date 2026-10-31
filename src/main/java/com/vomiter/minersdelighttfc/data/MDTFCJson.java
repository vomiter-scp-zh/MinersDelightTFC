package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

final class MDTFCJson {
    private MDTFCJson() {}

    static JsonElement ingredient(Ingredient ingredient) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
    }

    static JsonElement stack(ItemStack stack, HolderLookup.Provider registries) {
        return ItemStack.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), stack).getOrThrow();
    }
    static JsonElement provider(ItemStackProvider provider, HolderLookup.Provider registries) {
        return ItemStackProvider.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), provider).getOrThrow();
    }
}
