package com.vomiter.minersdelighttfc.registry;

import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.menu.CopperPotFluidMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MDTFCMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MinersDelightTFC.MOD_ID);

    public static final RegistryObject<MenuType<CopperPotFluidMenu>> COPPER_POT_FLUID =
            MENUS.register("copper_pot_fluid", () -> IForgeMenuType.create(CopperPotFluidMenu::new));

    private MDTFCMenuTypes() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
