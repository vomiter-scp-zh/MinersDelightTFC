package com.vomiter.minersdelighttfc.registry;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.menu.CopperPotFluidMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class MDTFCMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, MinersDelightTFC.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CopperPotFluidMenu>> COPPER_POT_FLUID =
            MENUS.register("copper_pot_fluid", () -> IMenuTypeExtension.create(CopperPotFluidMenu::new));

    private MDTFCMenuTypes() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
