package com.vomiter.minersdelighttfc.registry;

import com.sammy.minersdelight.setup.MDCreativeTabs;
import com.sammy.minersdelight.setup.MDItems;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.item.CopperCupItem;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MDTFCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS, MinersDelightTFC.MOD_ID
    );
    public static final RegistryObject<CopperCupItem> COPPER_CUP = ITEMS.register(
            "copper_cup", () -> new CopperCupItem(new Item.Properties().stacksTo(16))
    );

    private MDTFCItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(MDTFCItems::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(MDCreativeTabs.TAB_MINERS_DELIGHT.getKey())) {
            event.accept(COPPER_CUP.get());
        }
    }

    public static boolean isEmptyCopperCup(ItemStack stack) {
        // TFC exposes this capability only on a single-item stack.
        return stack.is(COPPER_CUP.get()) && stack.copyWithCount(1).getCapability(Capabilities.FLUID_ITEM)
                .map(handler -> handler.getFluidInTank(0).isEmpty()).orElse(false);
    }

    public static ItemStack replaceCopperCup(ItemStack stack) {
        if (!stack.is(MDItems.COPPER_CUP.get())) return stack;
        ItemStack replacement = new ItemStack(COPPER_CUP.get(), stack.getCount());
        if (stack.hasTag()) replacement.setTag(stack.getTag().copy());
        return replacement;
    }
}
