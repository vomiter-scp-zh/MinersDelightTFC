package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDItems;
import com.sammy.minersdelight.setup.MDTags;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** SD 3.7 client rules share TFC's existing sandwich recipe and stored meal ingredients. */
public final class MDTFCMealModelProvider implements DataProvider {
    private final PackOutput.PathProvider models;

    public MDTFCMealModelProvider(PackOutput output) {
        models = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "meal_models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        Ingredient cabbage = Ingredient.of(TFCItems.FOOD.get(Food.CABBAGE).get(), ModItems.CABBAGE_LEAF.get());
        Ingredient tomato = Ingredient.of(TFCItems.FOOD.get(Food.TOMATO).get());
        Ingredient onion = Ingredient.of(TFCItems.FOOD.get(Food.ONION).get());
        Ingredient tentacles = Ingredient.of(MDItems.BAKED_TENTACLES.get(),
                TFCItems.FOOD.get(Food.COOKED_CALAMARI).get());

        // TFC has three filling slots; condense the original four-filling burgers accordingly.
        save(cache, writes, MDItems.CAVE_HAMBURGER.getId(), 100,
                requirement(Ingredient.of(MDItems.VEGAN_PATTY.get(), MDItems.CRUNCHY_BAR.get()), 1),
                requirement(Ingredient.of(MDItems.SMOKED_BAT_WING.get()), 1),
                requirement(tentacles, 1));
        save(cache, writes, MDItems.VEGAN_HAMBURGER.getId(), 90,
                requirement(Ingredient.of(MDItems.VEGAN_PATTY.get()), 1),
                requirement(cabbage, 1), requirement(tomato, 1));
        save(cache, writes, MDItems.INSECT_WRAP.getId(), 80,
                requirement(Ingredient.of(MDItems.ARTHROPOD.get(), MDItems.SILVERFISH_EGGS.get()), 2));
        save(cache, writes, MDItems.INSECT_SANDWICH.getId(), 70,
                requirement(Ingredient.of(MDTags.COOKED_INSECT_MEAT), 1),
                requirement(Ingredient.of(MDItems.BAKED_SPIDER_LEG.get(), MDItems.COOKED_ARTHROPOD.get(),
                        MDItems.CRUNCHY_BAR.get(), MDItems.WEIRD_CAVIAR.get()), 2));
        save(cache, writes, MDItems.VEGAN_WRAP.getId(), 60,
                requirement(Ingredient.of(MDTags.BAKED_CAVE_CARROT), 1), requirement(onion, 1));
        save(cache, writes, MDItems.SQUID_SANDWICH.getId(), 0,
                requirement(Ingredient.of(MDItems.BAKED_SQUID.get(), MDItems.BAKED_TENTACLES.get(),
                        TFCItems.FOOD.get(Food.COOKED_CALAMARI).get()), 1));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private void save(CachedOutput cache, List<CompletableFuture<?>> writes, ResourceLocation item,
                      int priority, JsonObject... requirements) {
        JsonObject rule = new JsonObject();
        rule.add("meal", Ingredient.of(TFCItems.FOOD.get(Food.WHEAT_BREAD_SANDWICH).get()).toJson());
        rule.addProperty("priority", priority);
        rule.addProperty("model", item.getNamespace() + ":item/" + item.getPath());
        JsonArray ingredients = new JsonArray();
        for (JsonObject requirement : requirements) ingredients.add(requirement);
        rule.add("ingredients", ingredients);
        writes.add(DataProvider.saveStable(cache, rule, models.json(MinersDelightTFC.id(item.getPath()))));
    }

    private static JsonObject requirement(Ingredient ingredient, int min) {
        JsonObject requirement = new JsonObject();
        requirement.add("ingredient", ingredient.toJson());
        requirement.addProperty("min", min);
        return requirement;
    }

    @Override
    public String getName() {
        return "Miner's Delight TFC ingredient-based sandwich models";
    }
}
