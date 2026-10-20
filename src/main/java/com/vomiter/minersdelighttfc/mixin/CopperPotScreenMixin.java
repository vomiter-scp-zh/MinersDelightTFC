package com.vomiter.minersdelighttfc.mixin;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotScreen;
import com.vomiter.minersdelighttfc.client.screen.CopperPotFluidButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CopperPotScreen.class, remap = false)
public abstract class CopperPotScreenMixin extends AbstractContainerScreen<CopperPotMenu> {
    @Unique private CopperPotFluidButton mdtfc$fluidButton;

    protected CopperPotScreenMixin(CopperPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void mdtfc$addFluidButton(CallbackInfo ci) {
        mdtfc$fluidButton = new CopperPotFluidButton(leftPos + 7, height / 2 - 69, Component.translatable("gui.survivorsdelight.pot.fluid_menu"), menu);
        addRenderableWidget(mdtfc$fluidButton);
    }

    @Inject(method = "lambda$init$0(Lnet/minecraft/client/gui/components/Button;)V", at = @At("TAIL"), remap = false)
    private void mdtfc$moveFluidButton(Button button, CallbackInfo ci) {
        if (mdtfc$fluidButton != null) {
            mdtfc$fluidButton.setPosition(leftPos + 7, height / 2 - 69);
        }
    }
}
