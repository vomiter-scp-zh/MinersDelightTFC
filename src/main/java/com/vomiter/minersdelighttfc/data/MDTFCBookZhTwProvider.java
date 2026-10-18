package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDBlocks;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.data.book.SDPatchouliCategoryProvider;
import com.vomiter.survivorsdelight.data.book.TFCGuide;
import com.vomiter.survivorsdelight.data.book.builder.CategoryJson;
import com.vomiter.survivorsdelight.data.book.builder.EntryJson;
import com.vomiter.survivorsdelight.data.book.builder.TextBuilder;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class MDTFCBookZhTwProvider implements DataProvider {
    private static final String CATEGORY = "miners_delight_tfc";
    private static final String CATEGORY_ID = TerraFirmaCraft.MOD_ID + ":" + CATEGORY;
    private static final String ENTRIES = MinersDelightTFC.MOD_ID + "/";
    private static final String CAVE_CARROT = "cave_carrot";
    private static final String PIERCING = "piercing_harvests";
    private static final String SILVERFISH = "silverfish_infestation";
    private static final String COPPER_POT = "copper_pot";
    private static final String COPPER_CUP = "copper_cup";
    private static final String SD_COOKING_POT = "survivorsdelight/cooking_pot";
    private static final String LANGUAGE = "zh_tw";

    private final PackOutput output;

    public MDTFCBookZhTwProvider(PackOutput output) {
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
                .setName("礦工的餐桌")
                .setDescription("認識洞穴胡蘿蔔、穿刺採集的食材、地下食物箱的蠹魚侵擾，以及銅鍋與銅杯的用途。")
                .setIcon(itemId(MDItems.CAVE_CARROT.get()))
                .setSortnum(11)
                .build());
        saves.add(categories.run(cache));

        // SD's entry provider hardcodes the survivorsdelight/ entry prefix.
        // Reuse its builders, but keep this addon's entries at their own paths.
        PackOutput.PathProvider paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK,
                "patchouli_books/field_guide/" + LANGUAGE + "/entries/" + MinersDelightTFC.MOD_ID);
        entries.forEach((path, json) -> saves.add(DataProvider.saveStable(cache, json,
                paths.json(new ResourceLocation(TerraFirmaCraft.MOD_ID, path)))));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    private static JsonObject caveCarrot() {
        EntryJson.Builder builder = entry(CAVE_CARROT, "洞穴胡蘿蔔", MDItems.CAVE_CARROT.get(), 1)
                .addTextPage(TextBuilder.of("洞穴胡蘿蔔適合在地下栽種，是洞穴農田的一種食材來源。")
                        .p().text("在完全隔絕天空光的地方拔除普通胡蘿蔔作物，掉落的種子有 ").bold("70% 機率")
                        .text("變成洞穴胡蘿蔔種子。").toString())
                .addSpotlightPage(itemId(Items.TORCH), "洞穴深處或充分遮蔽的種植區，能提供種子轉變所需的環境。$(br2)你可以放置火把照明，方便查看作物、整理農田。")
                .addTextPage(TextBuilder.of("把種子種在").link(TFCGuide.Mechanics.MECHANICS_CROPS, "耕地")
                        .text("上，並為地下農田提供合適的溫度與").link(TFCGuide.Mechanics.MECHANICS_HYDRATION, "水分")
                        .text("。洞穴胡蘿蔔的生長條件與普通胡蘿蔔相同：")
                        .p().bold("溫度 3–30°C，水分 25–100%").text("。").toString())
                .addSpotlightPage(itemId(MDItems.CAVE_CARROT.get()),
                        "洞穴胡蘿蔔主要吸收耕地中的 $(bold)鉀$()。充足的鉀能改善生長速度與收成。$(br2)成熟後收取作物，可以得到洞穴胡蘿蔔，並回收種子繼續栽種。")
                .addTextPage("洞穴胡蘿蔔可以直接食用，也能加熱成烤洞穴胡蘿蔔。$(br2)你可以把烤洞穴胡蘿蔔做成排餅，或搭配洋蔥、煙燻蝙蝠翅與木棒製成串燒。它也能用於洞穴湯與其他蔬菜料理。")
                .addTextPage(TextBuilder.of("將新鮮的洞穴胡蘿蔔").link(TFCGuide.Mechanics.MECHANICS_HEATING, "加熱")
                        .text("至 ").bold("200°C").text("，即可得到烤洞穴胡蘿蔔。烤熟後可直接食用，也可作為其他料理的材料。").toString())
                .extraRecipeMapping(MDTFCItems.CAVE_CARROT_SEEDS, 0);
        JsonObject json = finish(builder, Map.of(MDItems.CAVE_CARROT.get(), 0, MDItems.BAKED_CAVE_CARROT.get(), 5),
                "取得種子", "地下的種植區", "種植條件", "鉀與收成", "食用與加工", "烤洞穴胡蘿蔔");
        // SD has no heating-page builder; use TFC's existing recipe template.
        JsonObject heating = json.getAsJsonArray("pages").get(5).getAsJsonObject();
        heating.addProperty("type", "tfc:heat_recipe");
        heating.addProperty("recipe", MinersDelightTFC.id("heating/cave_carrot").toString());
        return json;
    }

    private static JsonObject piercingHarvests() {
        EntryJson.Builder builder = entry(PIERCING, "穿刺與採集", MDItems.SQUID.get(), 2)
                .addTextPage(TextBuilder.of("標槍與小刀可以造成").link(TFCGuide.Mechanics.MECHANICS_DAMAGE_TYPES, "穿刺傷害")
                        .text("。用它們給獵物最後一擊，就有機會收集到可加工的食材。")
                        .p().text("蝙蝠翅、蜘蛛腿與完整魷魚，都能進一步加工與烹調。").toString())
                .addSpotlightPage(itemId(MDItems.ARTHROPOD.get()),
                        TextBuilder.of("穿刺擊殺蝙蝠可取得蝙蝠翅；蜘蛛與洞穴蜘蛛則能提供節肢動物肉。")
                        .p().text("普通蜘蛛還可能掉落蜘蛛腿。穿刺擊殺蠹魚能取得節肢動物肉，也有機會得到卵；另一種取得途徑，見")
                        .link(ENTRIES + SILVERFISH, "蠹魚侵擾").text("。").toString())
                .addTextPage("用穿刺武器獵取蜘蛛與洞穴蜘蛛，有機會收集到蜘蛛網，可用來加工成線或製作黏性籃子。$(br2)讓獵物著火後再以穿刺武器獵取，還可能取得熟節肢動物肉與烤蜘蛛腿。")
                .addSpotlightPage(itemId(MDItems.SPIDER_LEG.get()),
                        "把蜘蛛腿放在砧板上，用小刀切割，可以分出節肢動物肉。$(br2)也可以先加熱蜘蛛腿，再切出熟肉。蝙蝠翅則可先製成煙燻蝙蝠翅，供直接食用或繼續烹調。")
                .addTextPage("以穿刺武器獵取烏賊，可以取得完整魷魚；對章魚使用相同方式，則能取得完整發光魷魚。$(br2)你可以把它們切割、加熱，或用來製作鑲餡料理。")
                .addSpotlightPage(itemId(MDItems.GLOW_SQUID.get()),
                        "把新鮮的完整魷魚放上砧板，用小刀切割，可以得到魷魚肉與墨囊。$(br2)完整發光魷魚也能如此處理，取得魷魚肉與發光墨囊。發光墨囊可用於發光墨汁義大利麵。")
                .addTextPage("將完整魷魚或完整發光魷魚加熱成烤魷魚，再用小刀分切，就能得到烤觸手。$(br2)小份的生魷魚肉也可以切成生觸手，再加熱供後續料理使用。")
                .addSpotlightPage(itemId(MDItems.TENTACLES_ON_A_STICK.get()),
                        "兩份烤觸手或熟魷魚肉，可以與木棒製成觸手串。$(br2)魷魚肉也能搭配麵糰與洋蔥製作章魚燒。製作鑲餡料理時，則使用完整的魷魚。");
        return finish(builder, Map.ofEntries(
                        Map.entry(MDItems.BAT_WING.get(), 1), Map.entry(MDItems.SMOKED_BAT_WING.get(), 3),
                        Map.entry(MDItems.ARTHROPOD.get(), 1), Map.entry(MDItems.COOKED_ARTHROPOD.get(), 1),
                        Map.entry(MDItems.SPIDER_LEG.get(), 3), Map.entry(MDItems.BAKED_SPIDER_LEG.get(), 3),
                        Map.entry(MDItems.SQUID.get(), 4), Map.entry(MDItems.GLOW_SQUID.get(), 4),
                        Map.entry(MDItems.BAKED_SQUID.get(), 6), Map.entry(MDItems.TENTACLES.get(), 6),
                        Map.entry(MDItems.BAKED_TENTACLES.get(), 6), Map.entry(MDItems.TENTACLES_ON_A_STICK.get(), 7)),
                "用穿刺取得食材", "蝙蝠與蜘蛛", "蜘蛛網與熟肉", "採集後的處理",
                "取得完整魷魚", "分解完整魷魚", "烤魷魚與觸手", "串燒與鍋物");
    }

    private static JsonObject silverfish() {
        EntryJson.Builder builder = entry(SILVERFISH, "蠹魚侵擾", MDItems.SILVERFISH_EGGS.get(), 3)
                .addTextPage(TextBuilder.of("地底的糧食也會吸引").link(TFCGuide.Mechanics.MECHANICS_DECAY_PESTS, "害獸")
                        .text("。在地表下 ").bold("至少 10 格").text("的地方，存放食物的箱子可能引來蠹魚。")
                        .p().text("牠們會從箱子周圍出現，成為地下儲糧時可能遇上的害獸。").toString())
                .addSpotlightPage(itemId(MDItems.SILVERFISH_EGGS.get()),
                        "箱子距離地表越遠，一次侵擾可能引來的蠹魚就越多，最多可達八隻。$(br2)深入礦坑時，可以把食物裝進大缸並封口，減少害獸侵擾。")
                .addTextPage("食物箱引來的蠹魚會記住箱子的位置，平時會沿路返回箱子附近。$(br2)遇到敵人時，牠們會轉而追擊，之後再回到箱子旁。")
                .addSpotlightPage(itemId(MDItems.SILVERFISH_EGGS.get()),
                        "蠹魚約每五秒會搜尋一次身邊 $(bold)兩格內$() 的食物。$(br2)附近儲放食物的箱子或敞開的大缸，都可能因此引來更多害獸。")
                .addSpotlightPage(itemId(MDItems.SILVERFISH_EGGS.get()),
                        TextBuilder.of("蠹魚出現時，你也有機會在引來牠們的箱子裡找到蠹魚卵，每次最多 ")
                        .bold("五顆").text("。")
                        .p().text("卵會和食物一起留在箱子裡，供你取出收集。你也可以透過")
                        .link(ENTRIES + PIERCING, "穿刺採集").text("取得。").toString())
                .addTextPage(TextBuilder.of("箱子裡的食物容易吸引害獸。將食物裝進大缸並封口，可以妥善收藏地下的糧食。")
                        .p().text("更多儲存食物與防治害獸的方法，請參閱")
                        .link(TFCGuide.Mechanics.MECHANICS_DECAY_PESTS, "食物保鮮").text("。").toString());
        return finish(builder, Map.of(MDItems.SILVERFISH_EGGS.get(), 4),
                "地下的食物箱", "越深，數量越多", "回到箱子旁", "再次引發侵擾", "箱子裡的蠹魚卵", "食物的儲存方式");
    }

    private static JsonObject copperPot() {
        EntryJson.Builder builder = entry(COPPER_POT, "銅鍋", MDBlocks.COPPER_POT.get(), 4)
                .addTextPage(TextBuilder.of("加熱、加入液體與盛裝操作，請參閱").link(SD_COOKING_POT, "烹飪鍋")
                        .text("。銅鍋有四個食材欄位，能儲存 ").bold("4,000 mB").text(" 液體。食材更多的配方使用六格烹飪鍋。")
                        .p().text("煮好洞穴湯後，用空銅杯盛出餐點。銅杯的其他用途見")
                        .link(ENTRIES + COPPER_CUP, "銅杯").text("。").toString())
                .addAnvilRecipe(MinersDelightTFC.id("anvil/copper_pot"),
                        "一片銅雙層板可以在鐵砧上加工成一個銅鍋。依配方顯示的加工要求完成鍛造，再將鍋子放到有效熱源上。");
        return finish(builder, Map.of(MDBlocks.COPPER_POT.get(), 0), "銅鍋與烹飪鍋", "製作銅鍋");
    }

    private static JsonObject copperCup() {
        EntryJson.Builder builder = entry(COPPER_CUP, "銅杯", MDTFCItems.COPPER_CUP.get(), 5)
                .addTextPage("銅杯是可重複使用的容器，能像陶壺一樣舀取與攜帶液體，容量與可裝入的液體種類也和陶壺相同。$(br2)裝入飲料後，按住使用鍵即可飲用；喝完留下的空杯可以再次裝填。")
                .addAnvilRecipe(MinersDelightTFC.id("anvil/copper_cup"),
                        "將銅板在鐵砧上加工，可以製成空銅杯。它可以用來攜帶飲料，也能作為部分湯品的盛裝容器。")
                .addTextPage("先用空銅杯對可舀取的液體使用，再查看杯中的內容。杯內有液體時，物品外觀會顯示對應的液體顏色。$(br2)飲用後會喝完杯中的可飲用液體，再留下空杯。補水或其他飲用效果，依杯內的飲料而定。")
                .addSpotlightPage(itemId(MDItems.CAVE_SOUP_CUP.get()),
                        TextBuilder.of("用空銅杯從")
                        .link(ENTRIES + COPPER_POT, "銅鍋").text("盛出洞穴湯，就能得到可攜帶的杯裝餐點。")
                        .p().text("餐點的營養隨食材與分裝份量而變化，吃完後可以回收空杯。").toString());
        return finish(builder, Map.of(MDTFCItems.COPPER_CUP.get(), 0, MDItems.CAVE_SOUP_CUP.get(), 3),
                "可重複使用的杯子", "製作銅杯", "舀取與飲用", "飲料與杯裝餐點");
    }

    private static EntryJson.Builder entry(String path, String name, ItemLike icon, int sort) {
        return EntryJson.builder(path).setName(name).setCategory(CATEGORY_ID)
                .setIcon(itemId(icon)).setReadByDefault(true).setSortnum(sort);
    }

    private static String itemId(ItemLike item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).toString();
    }

    private static JsonObject finish(EntryJson.Builder builder, Map<? extends ItemLike, Integer> mappings,
                                     String... titles) {
        JsonObject json = builder.build().toJson();
        JsonArray pages = json.getAsJsonArray("pages");
        if (pages.size() != titles.length || pages.size() % 2 != 0) {
            throw new IllegalStateException("Field guide entries require one title per page and an even page count");
        }
        for (int i = 0; i < titles.length; i++) {
            pages.get(i).getAsJsonObject().addProperty("title", titles[i]);
        }
        // MD uses Registrate entries, and MDTFC's cup has a RegistryObject subtype.
        // SD's RegistryObject<Item> mapping overload cannot accept either directly.
        JsonObject extra = json.getAsJsonObject("extra_recipe_mappings");
        mappings.forEach((item, page) -> {
            if (page < 0 || page >= pages.size()) throw new IllegalArgumentException("Invalid book mapping page: " + page);
            extra.addProperty(itemId(item), page);
        });
        return json;
    }

    @Override
    public @NotNull String getName() {
        return "Miner's Delight TFC: Traditional Chinese field guide";
    }
}
