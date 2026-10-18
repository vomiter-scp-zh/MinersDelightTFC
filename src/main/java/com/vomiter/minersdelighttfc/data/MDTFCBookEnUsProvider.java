package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDBlocks;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.data.book.SDPatchouliCategoryProvider;
import com.vomiter.survivorsdelight.data.book.builder.CategoryJson;
import com.vomiter.survivorsdelight.data.book.builder.EntryJson;
import com.vomiter.survivorsdelight.data.book.builder.TextBuilder;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class MDTFCBookEnUsProvider implements DataProvider {
    private static final String CATEGORY = "miners_delight_tfc";
    private static final String CATEGORY_ID = TerraFirmaCraft.MOD_ID + ":" + CATEGORY;
    private static final String CAVE_CARROT = "cave_carrot";
    private static final String PIERCING = "piercing_harvests";
    private static final String SILVERFISH = "silverfish_infestation";
    private static final String COPPER_POT = "copper_pot";
    private static final String COPPER_CUP = "copper_cup";
    private static final String LANGUAGE = "en_us";

    private final PackOutput output;

    public MDTFCBookEnUsProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        var saves = new ArrayList<CompletableFuture<?>>();
        Map<String, JsonObject> entries = new LinkedHashMap<>();
        entries.put(CAVE_CARROT, caveCarrot());
        entries.put(PIERCING, piercingHarvests());
        entries.put(SILVERFISH, silverfish());
        entries.put(COPPER_POT, copperPot());
        entries.put(COPPER_CUP, copperCup());

        SDPatchouliCategoryProvider categories = new SDPatchouliCategoryProvider(output);
        categories.setLang(LANGUAGE);
        categories.category(CategoryJson.builder(CATEGORY)
                .setName("The Miner's Table")
                .setDescription("Learn about cave carrots, ingredients gathered with piercing weapons, silverfish around underground food chests, and the uses of copper pots and cups.")
                .setIcon(itemId(MDItems.CAVE_CARROT.get()))
                .setSortnum(11)
                .build());
        saves.add(categories.run(cache));

        // SD's entry provider hardcodes the survivorsdelight/ entry prefix.
        // Reuse its builders, but keep this addon's entries at their own paths.
        PackOutput.PathProvider paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,
                "patchouli_books/field_guide/" + LANGUAGE + "/entries/" + MinersDelightTFC.MOD_ID);
        entries.forEach((path, json) -> saves.add(DataProvider.saveStable(cache, json,
                paths.json(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, path)))));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private static JsonObject caveCarrot() {
        EntryJson.Builder builder = entry(CAVE_CARROT, "Cave Carrots", MDItems.CAVE_CARROT.get(), 1)
                .addTextPage(TextBuilder.of("Cave carrots are a protein-rich subspecies of ordinary carrots that has adapted to cave environment.").p().text("When you uproot ordinary carrot crops where skylight is completely blocked, their seeds have a $(bold)70% chance$() to become cave carrot seeds.").toString())
                .addTextPage("Grow Cave Carrots" ,"cult_cave_carrot", "Plant the seeds on $(l:mechanics/crops)farmland$() and provide suitable temperatures and $(l:mechanics/hydration)hydration$(). Cave carrots grow under the same conditions as ordinary carrots:$(br2)$(bold)Temperature: 3–30°C; hydration: 25–100%$().")
                .addSpotlightPage(itemId(MDItems.CAVE_CARROT.get()),
                        "Cave carrots draw $(bold)potassium$() from the soil. A good supply improves growth and harvests.$(br2)Harvest mature crops to gather cave carrots and recover seeds for replanting.")
                .addTextPage("Eat cave carrots fresh or heat them into baked cave carrots. They are also essential ingredients for many miner's dishes.")
                .extraRecipeMapping(MDTFCItems.CAVE_CARROT_SEEDS, 0);
        return builder.build().toJson();
    }

    private static JsonObject piercingHarvests() {
        EntryJson.Builder builder = entry(PIERCING, "Piercing and Gathering", MDItems.SQUID.get(), 2)
                .addTextPage("Javelins and knives deal $(l:mechanics/damage_types)piercing damage$(). Land the killing blow with one to gather ingredients for cooking.$(br2)Bat wings, spider legs, and whole squids can all be processed into food.")
                .addSpotlightPage(itemId(MDItems.ARTHROPOD.get()),
                        "Spiders and cave spiders provide arthropod meat.$(br2)Ordinary spiders may also drop spider legs. Silverfish killed with piercing damage yield arthropod meat and may drop eggs. For another source of eggs, see $(l:minersdelighttfc/silverfish_infestation)Silverfish Infestations$().")
                .addTextPage("You may also hunt spiders and cave spiders with piercing weapons for a chance to gather cobwebs, which can be processed into string or made into sticky baskets.")
                .addSpotlightPage(itemId(MDItems.GLOW_SQUID.get()),
                        "Hunt squid with piercing weapons to obtain whole squids. The same method yields whole glow squids from octopuses.$(br2)Cut them up, heat them, or use them to prepare stuffed dishes.");
        return builder.build().toJson();
    }

    private static JsonObject silverfish() {
        EntryJson.Builder builder = entry(SILVERFISH, "Silverfish Infestations", MDItems.SILVERFISH_EGGS.get(), 3)
                .addTextPage("Underground food supplies attract $(l:mechanics/decay#pests)pests$(), too. Food chests placed $(bold)at least 10 blocks$() below the surface may draw silverfish.$(br2)They emerge around the chest, making them a pest to watch for when storing food underground.")
                .addTextPage(
                        "The deeper a chest lies beneath the surface, the more silverfish an infestation may bring, up to eight at once." +
                                "$(br2)" +
                                "Silverfish drawn to a food chest remember its location and make their way back to it when there's no enemy to chase."
                )
                .addSpotlightPage(itemId(MDItems.SILVERFISH_EGGS.get()),
                        "When silverfish appear, you may also find silverfish eggs in the chest that attracted them, up to $(bold)five eggs$() at a time.$(br2)The eggs remain alongside the food, ready to collect. You can also obtain them through $(l:minersdelighttfc/piercing_harvests)piercing and gathering$().");
        return builder.build().toJson();
    }

    private static JsonObject copperPot() {
        EntryJson.Builder builder = entry(COPPER_POT, "Copper Pots", MDBlocks.COPPER_POT.get(), 4)
                .addTextPage("See $(l:survivorsdelight/cooking_pot)Cooking Pots$() for heating, adding liquids, and serving food. A copper pot has four ingredient slots and holds $(bold)4,000 mB$() of fluid. Use the six-slot cooking pot for recipes with more ingredients.$(br2)Serve finished cave soup into an empty copper cup. See $(l:minersdelighttfc/copper_cup)Copper Cups$() for its other uses.")
                .addAnvilRecipe(MinersDelightTFC.id("anvil/copper_pot"),
                        "Work one copper double sheet on an anvil to make a copper pot. Follow the recipe's forging requirements, then place the pot over a suitable heat source.");
        return builder.build().toJson();
    }

    private static JsonObject copperCup() {
        EntryJson.Builder builder = entry(COPPER_CUP, "Copper Cups", MDTFCItems.COPPER_CUP.get(), 5)
                .addTextPage("A copper cup is a reusable container for collecting, carrying and drinking liquids, much like a ceramic jug. It holds the same amount and accepts the same fluids as a jug.")
                .addAnvilRecipe(MinersDelightTFC.id("anvil/copper_cup"),
                        "Work a copper sheet on an anvil to make an empty copper cup. Use it to carry drinks or serve certain soups.")
                .addSpotlightPage(itemId(MDItems.CAVE_SOUP_CUP.get()),
                        "Use an empty copper cup to serve cave soup from a $(l:minersdelighttfc/copper_pot)copper pot$(), making a meal you can carry.$(br2)Its nutrition depends on the ingredients and portion size. Eating the meal returns the empty cup.");
        return builder.build().toJson();
    }

    private static EntryJson.Builder entry(String path, String name, ItemLike icon, int sort) {
        return EntryJson.builder(path).setName(name).setCategory(CATEGORY_ID)
                .setIcon(itemId(icon)).setReadByDefault(true).setSortnum(sort);
    }

    private static String itemId(ItemLike item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).toString();
    }

    @Override
    public @NotNull String getName() {
        return "Miner's Delight TFC: English field guide";
    }
}
