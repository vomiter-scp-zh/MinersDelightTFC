package com.vomiter.minersdelighttfc.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sammy.minersdelight.setup.MDBlocks;
import com.sammy.minersdelight.setup.MDItems;
import com.sammy.minersdelight.setup.MDTags;
import com.vomiter.minersdelighttfc.registry.MDTFCItems;
import com.vomiter.survivorsdelight.registry.SDItems;
import com.vomiter.survivorsdelight.data.recipe.builder.SDCuttingRecipeBuilder;
import com.vomiter.survivorsdelight.data.food.SDFoodAndRecipeGenerator;
import com.vomiter.survivorsdelight.data.food.SDFoodDataProvider;
import com.vomiter.survivorsdelight.data.recipe.builder.SDJsonAdapters;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.dries007.tfc.common.recipes.outputs.CopyFoodModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.data.event.GatherDataEvent;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.Objects;
import javax.annotation.Nullable;

public final class MDTFCCookingRecipeProvider extends RecipeProvider {
    private static final TagKey<Fluid> OILS = SDTags.FluidTags.COOKING_OILS;
    private static final TagKey<Item> TFC_BREADS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("tfc", "foods/breads"));

    private static final Ingredient BAKED_CAVE_CARROT = Ingredient.of(MDTags.BAKED_CAVE_CARROT);
    private static final Ingredient PASTA = Ingredient.of(CommonTags.Items.PASTA);
    private static final Ingredient BAT_WING = Ingredient.of(MDTags.BAT_WING);
    private static final Ingredient TFC_VEGETABLES = Ingredient.of(SDTags.ItemTags.TFC_VEGETABLES);
    private static final Ingredient ARTHROPOD_OR_EGGS = Ingredient.of(
            MDItems.ARTHROPOD.get(), MDItems.SILVERFISH_EGGS.get()
    );
    private static final Ingredient BAKED_SPIDER_LEG = Ingredient.of(MDItems.BAKED_SPIDER_LEG.get());
    private static final Ingredient SILVERFISH_EGGS = Ingredient.of(MDItems.SILVERFISH_EGGS.get());
    private static final Ingredient COOKED_ARTHROPOD = Ingredient.of(MDItems.COOKED_ARTHROPOD.get());
    private static final Ingredient COOKED_ARTHROPOD_OR_EGGS = Ingredient.of(
            MDItems.COOKED_ARTHROPOD.get(), MDItems.SILVERFISH_EGGS.get()
    );
    private static final Ingredient SOUPS = Ingredient.of(ItemTags.create(ResourceLocation.fromNamespaceAndPath("tfc","soups")));
    private static final Ingredient KNIVES = Ingredient.of(CommonTags.Items.TOOLS_KNIVES);
    private static final Ingredient SHEARS = Ingredient.of(Tags.Items.SHEARS);
    private static final Ingredient COOKED_TENTACLES = Ingredient.of(MDItems.BAKED_TENTACLES.get(),
            SDUtils.getTFCFoodItem(Food.COOKED_CALAMARI));
    private final String modid;
    private final SDFoodAndRecipeGenerator foodAndRecipes;

    public MDTFCCookingRecipeProvider(PackOutput output, String modid) {
        super(output);
        this.modid = Objects.requireNonNull(modid);
        this.foodAndRecipes = new SDFoodAndRecipeGenerator(modid);
        this.foodAndRecipes.injectPackOutput(output);
    }

    public static void gatherData(GatherDataEvent event, String modid) {
        var generator = event.getGenerator();
        var recipes = new MDTFCCookingRecipeProvider(generator.getPackOutput(), modid);
        generator.addProvider(event.includeServer(), recipes);
        generator.addProvider(event.includeServer(), recipes.foodAndRecipes.provider());
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> output) {
        saveBaseFoodData();
        saveIngredientRecipes(output);
        saveAssemblyRecipes(output);
        saveEffectFoodRecipes(output);
        saveUtilityRecipes(output);

        foodAndRecipes.cooking("bat_cookie", MDItems.BAT_COOKIE.get(), 8, 400, 2f, null)
                .food(MDItems.SMOKED_BAT_WING.get())
                .food(MDItems.SMOKED_BAT_WING.get())
                .food(SDTags.ItemTags.TFC_DOUGHS)
                .food(SDTags.ItemTags.TFC_DOUGHS)
                .build(output).saveFoodData();

        recipe("pasta_with_veggieballs", MDItems.PASTA_WITH_VEGGIEBALLS.get(), null, 400, 2F)
                .food(BAKED_CAVE_CARROT)
                .food(PASTA)
                .food(Ingredient.of(ModItems.TOMATO_SAUCE.get()))
                .fluid(OILS, 100).build(output).saveFoodData();

        recipe("cave_soup", MDItems.CAVE_SOUP.get(), null, 200, 1F)
                .food(Ingredient.of(MDTags.CAVE_CARROTS_VEGETABLE_ITEM))
                .food(Items.BROWN_MUSHROOM)
                .food(Items.RED_MUSHROOM)
                .food(SOUPS)
                .build(output).saveFoodData();

        recipe("bat_rolls", MDItems.BAT_ROLLS.get(), null, 200, 1F)
                .food(BAT_WING)
                .food(SDTags.ItemTags.TFC_RAW_MEATS)
                .build(output).saveFoodData();

        recipe("bat_soup", MDItems.BAT_SOUP.get(), null, 200, 1F)
                .food(BAT_WING)
                .food(BAT_WING)
                .food(SOUPS)
                .food(ModItems.BONE_BROTH.get())
                .build(output).saveFoodData();

        recipe("insect_stew", MDItems.INSECT_STEW.get(), null, 200, 1F)
                .food(SOUPS)
                .food(ARTHROPOD_OR_EGGS)
                .food(ARTHROPOD_OR_EGGS)
                .food(ARTHROPOD_OR_EGGS)
                .build(output).saveFoodData();

        recipe("alt_insect_stew", MDItems.INSECT_STEW.get(), null, 200, 1F)
                .food(Ingredient.of(MDItems.CRUNCHY_BAR.get()))
                .food(Ingredient.of(MDItems.ARTHROPOD.get(), MDItems.CRUNCHY_BAR.get()))
                .food(SOUPS)
                .build(output);

        recipe("fake_meatloaf", MDBlocks.FAKE_MEATLOAF.get(), Items.BOWL, 400, 2F)
                .food(BAKED_CAVE_CARROT)
                .food(BAKED_CAVE_CARROT)
                .food(Ingredient.of(MDItems.BAKED_CAVE_CARROT.get(), SDUtils.getTFCFoodItem(Food.CARROT)))
                .food(TFC_VEGETABLES)
                .fluid(OILS, 200).build(output).saveFoodData();

        recipe("takoyaki", MDItems.TAKOYAKI.get(), Items.BOWL, 200, 1F)
                .food(Food.CALAMARI)
                .food(Food.CALAMARI)
                .food(SDTags.ItemTags.TFC_DOUGHS)
                .food(Food.ONION)
                .build(output).saveFoodData();

        recipe("stuffed_squid", MDBlocks.STUFFED_SQUID.get(), Items.BOWL, 400, 2F)
                .food(Ingredient.of(MDTags.SQUID))
                .food(SDTags.ItemTags.TFC_GRAINS)
                .food(Ingredient.of(MDItems.CAVE_CARROT.get()))
                .food(Food.ONION)
                .food(TFC_VEGETABLES)
                .food(Food.BOILED_EGG)
                .fluid(OILS, 100).build(output).saveFoodData();

        recipe("glow_ink_pasta", MDItems.GLOW_INK_PASTA.get(), Items.BOWL, 400, 2F)
                .food(Food.CALAMARI)
                .food(PASTA)
                .food(SDTags.ItemTags.TFC_FRUITS)
                .nonfood(Ingredient.of(Items.GLOW_INK_SAC))
                .fluid(OILS, 100).build(output).saveFoodData();

        recipe("glazed_arachnid_limbs", MDBlocks.GLAZED_ARACHNID_LIMBS.get(), Items.BOWL, 400, 2F)
                .food(BAKED_SPIDER_LEG)
                .food(BAKED_SPIDER_LEG)
                .nonfood(SDTags.ItemTags.TFC_SWEETENER)
                .food(SDTags.ItemTags.TFC_GRAINS)
                .food(SDTags.ItemTags.TFC_FRUITS)
                .food(SDTags.ItemTags.TFC_FRUITS)
                .fluid(OILS, 100).build(output).saveFoodData();

        recipe("vegan_steak_and_potatoes", MDItems.VEGAN_STEAK_AND_POTATOES.get(), Items.BOWL, 300, 1.5F)
                .food(Food.BAKED_POTATO)
                .food(BAKED_CAVE_CARROT)
                .food(Food.ONION)
                .food(SDTags.ItemTags.TFC_GRAINS)
                .fluid(OILS, 100).build(output).saveFoodData();

        recipe("seasoned_arthropods", MDItems.SEASONED_ARTHROPODS.get(), Items.BOWL, 200, 1F)
                .food(Ingredient.of(MDItems.COOKED_ARTHROPOD.get(), MDItems.CRUNCHY_BAR.get()))
                .food(Ingredient.of(MDItems.WEIRD_CAVIAR.get()))
                .fluid(OILS, 100).build(output).saveFoodData();

        cut(Ingredient.of(MDItems.SPIDER_LEG.get()), true, KNIVES)
                .result(foodResult(MDItems.ARTHROPOD.get(), 2))
                .result(Items.BONE_MEAL, 1)
                .build(output, cuttingId("spider_leg"));

        cut(BAKED_SPIDER_LEG, true, KNIVES)
                .result(foodResult(MDItems.COOKED_ARTHROPOD.get(), 2))
                .result(Items.BONE_MEAL, 1)
                .build(output, cuttingId("baked_spider_leg"));

        cut(Ingredient.of(MDItems.BAKED_CAVE_CARROT.get()), true, KNIVES)
                .result(foodResult(MDItems.VEGAN_PATTY.get(), 2))
                .build(output, cuttingId("vegan_patty"));

        cut(Ingredient.of(MDItems.SQUID.get()), true, KNIVES)
                .result(foodResult(SDUtils.getTFCFoodItem(Food.CALAMARI), 3))
                .result(chanceResult(SDUtils.getTFCFoodItem(Food.CALAMARI), 1, 0.5F))
                .result(Items.INK_SAC, 1)
                .result(chanceResult(Items.INK_SAC, 2, 0.5F))
                .build(output, cuttingId("squid"));

        cut(Ingredient.of(MDItems.GLOW_SQUID.get()), true, KNIVES)
                .result(foodResult(SDUtils.getTFCFoodItem(Food.CALAMARI), 3))
                .result(chanceResult(SDUtils.getTFCFoodItem(Food.CALAMARI), 1, 0.5F))
                .result(Items.GLOW_INK_SAC, 1)
                .result(chanceResult(Items.GLOW_INK_SAC, 2, 0.5F))
                .build(output, cuttingId("glow_squid"));

        cut(Ingredient.of(MDItems.BAKED_SQUID.get()), true, KNIVES)
                .result(foodResult(MDItems.BAKED_TENTACLES.get(), 3))
                .result(chanceResult(MDItems.BAKED_TENTACLES.get(), 1, 0.5F))
                .build(output, cuttingId("baked_squid"));

        // TFC squid already drops calamari; expose the MD ingredient without adding meat.
        cut(Ingredient.of(SDUtils.getTFCFoodItem(Food.CALAMARI)), true, KNIVES)
                .result(foodResult(MDItems.TENTACLES.get(), 1))
                .build(output, cuttingId("tentacles"));

        cut(Ingredient.of(MDItems.BAT_WING.get()), false, KNIVES)
                .result(Items.PHANTOM_MEMBRANE, 1)
                .result(chanceResult(Items.PHANTOM_MEMBRANE, 2, 0.5F))
                .build(output, cuttingId("bat_wing"));

        cut(Ingredient.of(Items.MOSS_BLOCK), false, SHEARS)
                .result(MDItems.MOSS.get(), 4)
                .build(output, cuttingId("moss"));

        cut(Ingredient.of(MDBlocks.WILD_CAVE_CARROTS.get()), false, SHEARS)
                .result(MDBlocks.GOSSYPIUM.get().asItem(), 1)
                .result(chanceResult(MDBlocks.GOSSYPIUM.get(), 1, 0.5F))
                .result(MDItems.CAVE_CARROT.get(), 1)
                .result(chanceResult(MDItems.CAVE_CARROT.get(), 2, 0.5F))
                .build(output, cuttingId("wild_cave_carrot"));
    }

    private static SDCuttingRecipeBuilder cut(Ingredient ingredient, boolean notRotten, Ingredient tool) {
        SDCuttingRecipeBuilder builder = SDCuttingRecipeBuilder.cutting().tool(tool);
        return notRotten ? builder.notRotten(ingredient) : builder.ingredient(ingredient);
    }

    private static JsonElement foodResult(ItemLike item, int count) {
        return SDJsonAdapters.writeISP(ItemStackProvider.of(
                new ItemStack(item, count), CopyFoodModifier.INSTANCE
        ));
    }

    private static JsonObject chanceResult(ItemLike item, int count, float chance) {
        JsonObject result = SDJsonAdapters.stackToJson(new ItemStack(item, count));
        result.addProperty("chance", chance);
        return result;
    }

    private SDFoodAndRecipeGenerator.CookingBuilder recipe(
            String path, ItemLike result, @Nullable ItemLike container, int time, float experience
    ) {
        return foodAndRecipes.cooking(path, result, 1, time, experience, container);
    }

    private void saveBaseFoodData() {
        SDFoodDataProvider food = foodAndRecipes.provider();

        food.newBuilder("raw/cave_carrot").item(MDItems.CAVE_CARROT.get())
                .from(Food.CARROT)
                .setProtein(0.6)
                .save();
        food.newBuilder("raw/baked_cave_carrot").item(MDItems.BAKED_CAVE_CARROT.get())
                .from(Food.CARROT)
                .setProtein(0.9)
                .save();
        food.newBuilder("raw/bat_wing").item(MDItems.BAT_WING.get())
                .slicedFrom(Food.RABBIT, 2).save();
        food.newBuilder("raw/smoked_bat_wing").item(MDItems.SMOKED_BAT_WING.get())
                .slicedFrom(Food.COOKED_RABBIT, 2).save();
        food.newBuilder("raw/spider_leg").item(MDItems.SPIDER_LEG.get())
                .multipliedFrom(Food.SHELLFISH, 2).save();
        food.newBuilder("raw/baked_spider_leg").item(MDItems.BAKED_SPIDER_LEG.get())
                .multipliedFrom(Food.COOKED_SHELLFISH, 2).save();
        food.newBuilder("raw/arthropod").item(MDItems.ARTHROPOD.get())
                .from(Food.SHELLFISH).save();
        food.newBuilder("raw/cooked_arthropod").item(MDItems.COOKED_ARTHROPOD.get())
                .from(Food.COOKED_SHELLFISH).save();
        food.newBuilder("raw/silverfish_eggs").item(MDItems.SILVERFISH_EGGS.get())
                .slicedFrom(Food.COOKED_EGG, 2).save();
        food.newBuilder("raw/squid").item(MDItems.SQUID.get())
                .multipliedFrom(Food.CALAMARI, 3).save();
        food.newBuilder("raw/glow_squid").item(MDItems.GLOW_SQUID.get())
                .multipliedFrom(Food.CALAMARI, 3).save();
        food.newBuilder("raw/baked_squid").item(MDItems.BAKED_SQUID.get())
                .multipliedFrom(Food.COOKED_CALAMARI, 3).save();
        food.newBuilder("raw/tentacles").item(MDItems.TENTACLES.get())
                .from(Food.CALAMARI).save();
        food.newBuilder("raw/baked_tentacles").item(MDItems.BAKED_TENTACLES.get())
                .from(Food.COOKED_CALAMARI).save();
        food.newBuilder("cut/moss").item(MDItems.MOSS.get())
                .slicedFrom(Food.DRIED_SEAWEED, 4).save();

        food.newBuilder("cut/vegan_patty").item(MDItems.VEGAN_PATTY.get())
                .type("dynamic").save();
        food.newBuilder("servings/fake_meatloaf").item(MDItems.PLATE_OF_FAKE_MEATLOAF.get())
                .type("dynamic").save();
        food.newBuilder("servings/stuffed_squid").item(MDItems.BOWL_OF_STUFFED_SQUID.get())
                .type("dynamic").save();
        food.newBuilder("servings/glazed_arachnid_limbs").item(MDItems.PLATE_OF_GLAZED_ARACHNID_LIMBS.get())
                .type("dynamic").save();

        // Preserve food capabilities on existing MD meals. New sandwiches use TFC meals and client model rules.
        food.newBuilder("vegan_hamburger").item(MDItems.VEGAN_HAMBURGER.get())
                .type("dynamic").save();
        food.newBuilder("vegan_wrap").item(MDItems.VEGAN_WRAP.get())
                .type("dynamic").save();
        food.newBuilder("cave_hamburger").item(MDItems.CAVE_HAMBURGER.get())
                .type("dynamic").save();
        food.newBuilder("insect_sandwich").item(MDItems.INSECT_SANDWICH.get())
                .type("dynamic").save();
        food.newBuilder("insect_wrap").item(MDItems.INSECT_WRAP.get())
                .type("dynamic").save();
        food.newBuilder("squid_sandwich").item(MDItems.SQUID_SANDWICH.get())
                .type("dynamic").save();

        food.newBuilder("cup/cave_soup").item(MDItems.CAVE_SOUP_CUP.get())
                .type("dynamic").save();
        food.newBuilder("cup/chicken_soup").item(MDItems.CHICKEN_SOUP_CUP.get())
                .type("dynamic").save();


    }

    private void saveIngredientRecipes(Consumer<FinishedRecipe> output) {
        // These prepared ingredients must also compute nutrition rather than stay at zero.
        foodAndRecipes.craftingShapeless("weird_caviar", MDItems.WEIRD_CAVIAR.get(), 1)
                .requires(Items.BOWL)
                .requires(NotRottenIngredient.of(SILVERFISH_EGGS), 3)
                .build(output).saveFoodData();

        foodAndRecipes.craftingShapeless("crunchy_bar", MDItems.CRUNCHY_BAR.get(), 4)
                .requires(NotRottenIngredient.of(COOKED_ARTHROPOD), 3)
                .requires(NotRottenIngredient.of(COOKED_ARTHROPOD_OR_EGGS))
                .requires(NotRottenIngredient.of(SILVERFISH_EGGS), 3)
                .build(output).saveFoodData();
    }

    private void saveAssemblyRecipes(Consumer<FinishedRecipe> output) {
        // Sandwich appearances are selected by MDTFCMealModelProvider from TFC meal ingredients.

        foodAndRecipes.craftingShapeless("improvised_barbecue_stick", MDItems.IMPROVISED_BARBECUE_STICK.get(), 1)
                .requires(fresh(BAKED_CAVE_CARROT))
                .requires(fresh(Ingredient.of(SDUtils.getTFCFoodItem(Food.ONION))))
                .requires(fresh(Ingredient.of(MDItems.SMOKED_BAT_WING.get())))
                .requires(Tags.Items.RODS_WOODEN)
                .build(output).saveFoodData();

        foodAndRecipes.craftingShapeless("tentacles_on_a_stick", MDItems.TENTACLES_ON_A_STICK.get(), 1)
                .requires(Tags.Items.RODS_WOODEN)
                .requires(fresh(COOKED_TENTACLES), 2)
                .build(output).saveFoodData();
    }

    private void saveEffectFoodRecipes(Consumer<FinishedRecipe> output) {
        // Follow SD's golden carrot: one fresh food plus one leaf, preserving nutrition.
        foodAndRecipes.craftingShapeless("copper_carrot", MDItems.COPPER_CARROT.get(), 1)
                .requires(fresh(Ingredient.of(MDItems.CAVE_CARROT.get())))
                .requires(MDTFCItems.COPPER_LEAF.get())
                .build(output).saveFoodData();

        // Halve the original batch so its ingredients fit the pot's six input slots.
        foodAndRecipes.cooking("nutritional_bar", MDItems.NUTRITIONAL_BAR.get(), 2, 200, 1F, null)
                .food(MDItems.CRUNCHY_BAR.get())
                .food(MDItems.CRUNCHY_BAR.get())
                .food(SDTags.ItemTags.TFC_GRAINS)
                .nonfood(SDTags.ItemTags.TFC_SWEETENER)
                .build(output).saveFoodData();

        foodAndRecipes.craftingShapeless("golden_nutritional_bar", MDItems.GOLDEN_NUTRITIONAL_BAR.get(), 1)
                .requires(fresh(Ingredient.of(MDItems.NUTRITIONAL_BAR.get())))
                .requires(SDItems.GOLD_FLAKE.get())
                .build(output).saveFoodData();
    }

    private void saveUtilityRecipes(Consumer<FinishedRecipe> output) {

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, MDBlocks.STICKY_BASKET.get())
                .pattern("X X").pattern("Y Y").pattern("XYX")
                .define('X', Tags.Items.RODS_WOODEN)
                .define('Y', Items.COBWEB)
                .unlockedBy("has_cobweb", has(Items.COBWEB))
                .save(output, ResourceLocation.fromNamespaceAndPath(modid, "crafting/sticky_basket"));
    }

    private static Ingredient fresh(Ingredient ingredient) {
        return NotRottenIngredient.of(ingredient);
    }

    private ResourceLocation cuttingId(String path) {
        return ResourceLocation.fromNamespaceAndPath(modid, "cutting/" + path);
    }
}
