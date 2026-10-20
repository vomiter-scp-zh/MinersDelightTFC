package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class MDTFCDisabledRecipesProvider implements DataProvider {
    private static final String MD = "miners_delight";
    private final PackOutput.PathProvider recipes;
    private final String namespace;

    public MDTFCDisabledRecipesProvider(PackOutput output, String namespace) {
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.namespace = namespace;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = ModList.get().getModFileById(MD).getFile().findResource("data", namespace, "recipes");
        JsonObject disabled = new JsonObject();
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "forge:false");
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        disabled.add("conditions", conditions);
        var saves = new ArrayList<CompletableFuture<?>>();
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                String path = root.relativize(file).toString().replace('\\', '/');
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path.substring(0, path.length() - 5));
                saves.add(DataProvider.saveStable(cache, disabled, recipes.json(id)));
            }
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Disable every original Miner's Delight recipe under namespace "+ namespace;
    }
}
