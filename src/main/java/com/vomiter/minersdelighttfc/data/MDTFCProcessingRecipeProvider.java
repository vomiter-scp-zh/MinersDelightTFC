package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDBlocks;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.survivorsdelight.data.recipe.builder.SDJsonAdapters;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.dries007.tfc.common.recipes.outputs.CopyFoodModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.Metal;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class MDTFCProcessingRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider heats;

    public MDTFCProcessingRecipeProvider(PackOutput output) {
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        heats = output.createPathProvider(PackOutput.Target.DATA_PACK, "tfc/item_heats");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var saves = new ArrayList<CompletableFuture<?>>();
        List<Heating> heating = List.of(
                new Heating("cave_carrot", MDItems.CAVE_CARROT.get(), MDItems.BAKED_CAVE_CARROT.get()),
                new Heating("bat_wing", MDItems.BAT_WING.get(), MDItems.SMOKED_BAT_WING.get()),
                new Heating("spider_leg", MDItems.SPIDER_LEG.get(), MDItems.BAKED_SPIDER_LEG.get()),
                new Heating("arthropod", MDItems.ARTHROPOD.get(), MDItems.COOKED_ARTHROPOD.get()),
                new Heating("squid", MDItems.SQUID.get(), MDItems.BAKED_SQUID.get()),
                new Heating("glow_squid_to_baked_squid", MDItems.GLOW_SQUID.get(), MDItems.BAKED_SQUID.get()),
                new Heating("tentacles", MDItems.TENTACLES.get(), MDItems.BAKED_TENTACLES.get())
        );
        for (Heating entry : heating) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "tfc:heating");
            json.add("ingredient", NotRottenIngredient.of(Ingredient.of(entry.input())).toJson());
            json.add("result_item", SDJsonAdapters.writeISP(ItemStackProvider.of(
                    new ItemStack(entry.result()), CopyFoodModifier.INSTANCE)));
            json.addProperty("temperature", 200);
            saves.add(DataProvider.saveStable(cache, json, recipes.json(MinersDelightTFC.id("heating/" + entry.path()))));
        }
        JsonObject heat = new JsonObject();
        heat.add("ingredient", Ingredient.of(heating.stream()
                .flatMap(entry -> Stream.of(entry.input(), entry.result())).distinct()
                .toArray(ItemLike[]::new)).toJson());
        heat.addProperty("heat_capacity", 1.0);
        saves.add(DataProvider.saveStable(cache, heat, heats.json(MinersDelightTFC.id("basic_ingredients"))));

        JsonObject pot = new JsonObject();
        pot.addProperty("type", "tfc:anvil");
        pot.add("input", Ingredient.of(TFCItems.METAL_ITEMS.get(Metal.Default.COPPER)
                .get(Metal.ItemType.DOUBLE_SHEET).get()).toJson());
        pot.add("result", SDJsonAdapters.stackToJson(new ItemStack(MDBlocks.COPPER_POT.get())));
        pot.addProperty("tier", 1);
        JsonArray rules = new JsonArray();
        rules.add("bend_last");
        rules.add("bend_second_last");
        rules.add("bend_third_last");
        pot.add("rules", rules);
        saves.add(DataProvider.saveStable(cache, pot, recipes.json(MinersDelightTFC.id("anvil/copper_pot"))));

        JsonObject spinning = new JsonObject();
        spinning.addProperty("type", "tfc:damage_inputs_shapeless_crafting");
        JsonObject inner = new JsonObject();
        inner.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ingredients = new JsonArray();
        ingredients.add(Ingredient.of(TFCItems.SPINDLE.get()).toJson());
        ingredients.add(Ingredient.of(MDBlocks.GOSSYPIUM.get()).toJson());
        inner.add("ingredients", ingredients);
        inner.add("result", SDJsonAdapters.stackToJson(new ItemStack(Items.STRING, 2)));
        spinning.add("recipe", inner);
        saves.add(DataProvider.saveStable(cache, spinning, recipes.json(MinersDelightTFC.id("crafting/string_from_gossypium"))));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Miner's Delight TFC heating, metalworking and spinning";
    }

    private record Heating(String path, ItemLike input, ItemLike result) {}
}
