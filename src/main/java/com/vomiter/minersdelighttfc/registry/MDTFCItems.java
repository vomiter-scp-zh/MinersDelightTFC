package com.vomiter.minersdelighttfc.registry;

import com.sammy.minersdelight.setup.MDCreativeTabs;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.item.CopperCupItem;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class MDTFCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            BuiltInRegistries.ITEM, MinersDelightTFC.MOD_ID
    );
    public static final DeferredHolder<Item, CopperCupItem> COPPER_CUP = ITEMS.register(
            "copper_cup", () -> new CopperCupItem(new Item.Properties().stacksTo(16))
    );
    public static final DeferredHolder<Item, Item> COPPER_LEAF = ITEMS.register("copper_leaf",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CAVE_CARROT_SEEDS = ITEMS.register("seeds/cave_carrot",
            () -> new ItemNameBlockItem(MDTFCBlocks.CAVE_CARROT.get(), new Item.Properties()));

    private MDTFCItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(MDTFCItems::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(MDCreativeTabs.CONTENT.getKey())) {
            event.accept(COPPER_CUP.get());
            event.accept(COPPER_LEAF.get());
            event.accept(CAVE_CARROT_SEEDS.get());
        }
    }

    public static boolean isEmptyCopperCup(ItemStack stack) {
        // TFC exposes this capability only on a single-item stack.
        var handler = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        return stack.is(COPPER_CUP.get()) && handler != null && handler.getFluidInTank(0).isEmpty();
    }

    public static ItemStack replaceCopperCup(ItemStack stack) {
        if (!stack.is(MDItems.COPPER_CUP.get())) return stack;
        ItemStack replacement = new ItemStack(COPPER_CUP.get(), stack.getCount());
        replacement.applyComponents(stack.getComponentsPatch());
        return replacement;
    }
}
