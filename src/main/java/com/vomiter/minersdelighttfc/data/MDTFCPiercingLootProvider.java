package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class MDTFCPiercingLootProvider implements DataProvider {
    private static final String MD = "miners_delight";
    private final PackOutput.PathProvider output;

    public MDTFCPiercingLootProvider(PackOutput output) {
        this.output = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_modifiers");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = ModList.get().getModFileById(MD).getFile()
                .findResource("data", MD, "loot_modifiers");
        var saves = new ArrayList<CompletableFuture<?>>();
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                JsonObject json;
                try (Reader reader = Files.newBufferedReader(file)) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                }
                if (!rewriteKnifeConditions(json)) continue;
                String path = root.relativize(file).toString().replace('\\', '/');
                ResourceLocation id = new ResourceLocation(MD, path.substring(0, path.length() - 5));
                saves.add(DataProvider.saveStable(cache, json, output.json(id)));
            }
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        if (saves.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("No Miner's Delight knife loot found in " + root));
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private static boolean rewriteKnifeConditions(JsonElement element) {
        boolean changed = false;
        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
            if (json.has("condition") && json.get("condition").getAsString().equals("minecraft:entity_properties")
                    && json.has("entity") && json.get("entity").getAsString().equals("killer")
                    && json.has("predicate")) {
                JsonObject predicate = json.getAsJsonObject("predicate");
                if (predicate.has("equipment") && predicate.getAsJsonObject("equipment").has("mainhand")) {
                    JsonObject hand = predicate.getAsJsonObject("equipment").getAsJsonObject("mainhand");
                    if (hand.has("tag") && hand.get("tag").getAsString().equals("farmersdelight:tools/knives")) {
                        json.remove("entity");
                        json.remove("predicate");
                        json.addProperty("condition", "minersdelighttfc:piercing_damage");
                        return true;
                    }
                }
            }
            // This backport shipped the 1.21 condition name in several modifiers.
            if (json.has("condition") && json.get("condition").getAsString().equals("minecraft:any_of")) {
                json.addProperty("condition", "minecraft:alternative");
            }
            for (var entry : json.entrySet()) changed |= rewriteKnifeConditions(entry.getValue());
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) changed |= rewriteKnifeConditions(child);
        }
        return changed;
    }

    @Override
    public String getName() {
        return "Miner's Delight knife loot using TFC piercing damage";
    }
}
