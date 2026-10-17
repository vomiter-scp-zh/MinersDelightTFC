package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class MDTFCCopperCupDataProvider implements DataProvider {
    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider models;
    private final boolean server;
    private final boolean client;

    public MDTFCCopperCupDataProvider(PackOutput output, boolean server, boolean client) {
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        models = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        this.server = server;
        this.client = client;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        var saves = new ArrayList<CompletableFuture<?>>();
        if (server) {
            JsonObject recipe = new JsonObject();
            recipe.addProperty("type", "tfc:anvil");
            JsonObject input = new JsonObject();
            input.addProperty("tag", "forge:sheets/copper");
            recipe.add("input", input);
            JsonObject result = new JsonObject();
            result.addProperty("item", ForgeRegistries.ITEMS.getKey(MDTFCItems.COPPER_CUP.get()).toString());
            recipe.add("result", result);
            recipe.addProperty("tier", 1);
            JsonArray rules = new JsonArray();
            rules.add("bend_last");
            rules.add("bend_second_last");
            rules.add("bend_third_last");
            recipe.add("rules", rules);
            saves.add(DataProvider.saveStable(output, recipe,
                    recipes.json(MinersDelightTFC.id("anvil/copper_cup"))));
        }
        if (client) {
            JsonObject model = new JsonObject();
            model.addProperty("loader", "tfc:contained_fluid");
            model.addProperty("parent", "forge:item/default");
            JsonObject textures = new JsonObject();
            textures.addProperty("base", "miners_delight:item/copper_cup");
            textures.addProperty("fluid", "minersdelighttfc:item/copper_cup_fluid");
            model.add("textures", textures);
            saves.add(DataProvider.saveStable(output, model,
                    models.json(MinersDelightTFC.id("item/copper_cup"))));
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Miner's Delight TFC copper cup";
    }
}
