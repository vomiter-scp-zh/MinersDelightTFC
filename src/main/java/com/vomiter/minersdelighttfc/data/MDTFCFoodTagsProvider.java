package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/** Classify ingredients so TFC recipes and food preservation use the same groups. */
public final class MDTFCFoodTagsProvider implements DataProvider {
    private static final List<ResourceLocation> RAW_MEATS = List.of(
            MDItems.BAT_WING.getId(), MDItems.SPIDER_LEG.getId(), MDItems.ARTHROPOD.getId(),
            MDItems.SQUID.getId(), MDItems.GLOW_SQUID.getId(), MDItems.TENTACLES.getId());
    private static final List<ResourceLocation> COOKED_MEATS = List.of(
            MDItems.SMOKED_BAT_WING.getId(), MDItems.BAKED_SPIDER_LEG.getId(),
            MDItems.COOKED_ARTHROPOD.getId(), MDItems.BAKED_SQUID.getId(), MDItems.BAKED_TENTACLES.getId());
    private static final List<ResourceLocation> VEGETABLES = List.of(
            MDItems.CAVE_CARROT.getId(), MDItems.BAKED_CAVE_CARROT.getId(),
            MDItems.VEGAN_PATTY.getId(), MDItems.MOSS.getId());
    // Eggs and mixed preparations remain distinct from plain meat and vegetables.
    private static final List<ResourceLocation> SALAD_INGREDIENTS = List.of(
            MDItems.SILVERFISH_EGGS.getId(), MDItems.CRUNCHY_BAR.getId());
    private static final List<ResourceLocation> OTHER_INGREDIENTS = Stream.concat(
            SALAD_INGREDIENTS.stream(), Stream.of(MDItems.WEIRD_CAVIAR.getId())).toList();

    private final PackOutput.PathProvider tags;

    public MDTFCFoodTagsProvider(PackOutput output) {
        tags = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        save(cache, writes, "foods/raw_meats", RAW_MEATS);
        save(cache, writes, "foods/cooked_meats", COOKED_MEATS);
        // TFC 3.2.18 lists both raw and cooked meat explicitly in this tag.
        save(cache, writes, "foods/meats", Stream.concat(RAW_MEATS.stream(), COOKED_MEATS.stream()).toList());
        save(cache, writes, "foods/vegetables", VEGETABLES);
        save(cache, writes, "foods", Stream.of(RAW_MEATS, COOKED_MEATS, VEGETABLES, OTHER_INGREDIENTS)
                .flatMap(List::stream).toList());
        // Base TFC tags already include vegetables and cooked meats in salads and sandwiches,
        // and meats in soup. Only additional ingredients need direct entries here.
        save(cache, writes, "foods/usable_in_soup", OTHER_INGREDIENTS);
        // SaladContainer shrinks inputs without returning containers. Keep bowl food in soup
        // and crafting recipes, which handle crafting remainders.
        save(cache, writes, "foods/usable_in_salad", SALAD_INGREDIENTS);
        // Retain the previous model patch's raw arthropod filling for insect wraps.
        save(cache, writes, "foods/usable_in_sandwich", Stream.concat(
                OTHER_INGREDIENTS.stream(), Stream.of(MDItems.ARTHROPOD.getId())).toList());
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private void save(CachedOutput cache, List<CompletableFuture<?>> writes,
                      String path, List<ResourceLocation> items) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);
        JsonArray values = new JsonArray();
        items.forEach(item -> values.add(item.toString()));
        tag.add("values", values);
        writes.add(DataProvider.saveStable(cache, tag,
                tags.json(ResourceLocation.fromNamespaceAndPath("tfc", path))));
    }

    @Override
    public String getName() {
        return "Miner's Delight TFC food ingredient tags";
    }
}
