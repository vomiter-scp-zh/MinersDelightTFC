package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDBlocks;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import net.minecraft.core.HolderLookup;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.dries007.tfc.common.recipes.ingredients.AndIngredient;
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
    private final CompletableFuture<HolderLookup.Provider> lookup;
    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider heats;

    public MDTFCProcessingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        this.lookup = lookup;
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        heats = output.createPathProvider(PackOutput.Target.DATA_PACK, "tfc/item_heat");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookup.thenCompose(registries -> {
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
            json.add("ingredient", MDTFCJson.ingredient(AndIngredient.of(Ingredient.of(entry.input()), NotRottenIngredient.INSTANCE)));
            json.add("result_item", MDTFCJson.provider(ItemStackProvider.of(
                    new ItemStack(entry.result()), CopyFoodModifier.INSTANCE), registries));
            json.addProperty("temperature", 200);
            saves.add(DataProvider.saveStable(cache, json, recipes.json(MinersDelightTFC.id("heating/" + entry.path()))));
        }
        JsonObject heat = new JsonObject();
        heat.add("ingredient", MDTFCJson.ingredient(Ingredient.of(heating.stream()
                .flatMap(entry -> Stream.of(entry.input(), entry.result())).distinct()
                .toArray(ItemLike[]::new))));
        heat.addProperty("heat_capacity", 1.0);
        saves.add(DataProvider.saveStable(cache, heat, heats.json(MinersDelightTFC.id("basic_ingredients"))));

        JsonObject pot = new JsonObject();
        pot.addProperty("type", "tfc:anvil");
        pot.add("ingredient", MDTFCJson.ingredient(Ingredient.of(TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.DOUBLE_SHEET).get())));
        pot.add("result", MDTFCJson.stack(new ItemStack(MDBlocks.COPPER_POT.get()), registries));
        pot.addProperty("tier", 1);
        JsonArray rules = new JsonArray();
        rules.add("bend_last");
        rules.add("bend_second_last");
        rules.add("bend_third_last");
        pot.add("rules", rules);
        saves.add(DataProvider.saveStable(cache, pot, recipes.json(MinersDelightTFC.id("anvil/copper_pot"))));

        // Match SD gold flakes: one ingot, tier 0, three final draws, four leaves.
        JsonObject leaf = new JsonObject();
        leaf.addProperty("type", "tfc:anvil");
        leaf.add("ingredient", MDTFCJson.ingredient(Ingredient.of(TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.INGOT).get())));
        leaf.add("result", MDTFCJson.stack(new ItemStack(MDTFCItems.COPPER_LEAF.get(), 4), registries));
        leaf.addProperty("tier", 0);
        JsonArray leafRules = new JsonArray();
        leafRules.add("draw_third_last");
        leafRules.add("draw_second_last");
        leafRules.add("draw_last");
        leaf.add("rules", leafRules);
        leaf.addProperty("apply_forging_bonus", false);
        saves.add(DataProvider.saveStable(cache, leaf, recipes.json(MinersDelightTFC.id("anvil/copper_leaf"))));

        JsonObject spinning = new JsonObject();
        spinning.addProperty("type", "tfc:advanced_shapeless_crafting");
        JsonArray ingredients = new JsonArray();
        ingredients.add(MDTFCJson.ingredient(Ingredient.of(TFCItems.SPINDLE.get())));
        ingredients.add(MDTFCJson.ingredient(Ingredient.of(MDBlocks.GOSSYPIUM.get())));
        spinning.add("ingredients", ingredients);
        spinning.add("primary_ingredient", MDTFCJson.ingredient(Ingredient.of(TFCItems.SPINDLE.get())));
        JsonObject remainder = new JsonObject();
        JsonArray modifiers = new JsonArray();
        JsonObject damage = new JsonObject();
        damage.addProperty("type", "tfc:damage_crafting_remainder");
        modifiers.add(damage);
        remainder.add("modifiers", modifiers);
        spinning.add("remainder", remainder);
        spinning.add("result", MDTFCJson.stack(new ItemStack(Items.STRING, 2), registries));
        saves.add(DataProvider.saveStable(cache, spinning, recipes.json(MinersDelightTFC.id("crafting/string_from_gossypium"))));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Miner's Delight TFC heating, metalworking and spinning";
    }

    private record Heating(String path, ItemLike input, ItemLike result) {}
}
