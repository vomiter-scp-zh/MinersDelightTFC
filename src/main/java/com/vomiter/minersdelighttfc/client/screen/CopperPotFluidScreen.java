package com.vomiter.minersdelighttfc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sammy.minersdelight.setup.MDBlocks;
import com.vomiter.minersdelighttfc.MinersDelightTFC;
import com.vomiter.minersdelighttfc.menu.CopperPotFluidMenu;
import com.vomiter.minersdelighttfc.network.MDTFCNetwork;
import com.vomiter.minersdelighttfc.network.packet.OpenCopperPotMenuC2SPacket;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public final class CopperPotFluidScreen extends AbstractContainerScreen<CopperPotFluidMenu> {
    private static final ResourceLocation BACKGROUND =
            MinersDelightTFC.id("textures/gui/copper_pot_fluid.png");
    private static final ResourceLocation COOKING_POT_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "textures/gui/cooking_pot.png");

    public CopperPotFluidScreen(CopperPotFluidMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(new BackButton(leftPos + 7, height / 2 - 69));
    }

    @Override public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int progress = menu.getCookProgressionScaled();
        graphics.blit(COOKING_POT_BACKGROUND, leftPos + 89, topPos + 25, 176, 15, progress + 1, 17);
        FluidStack fluid = menu.getFluid();
        if (!fluid.isEmpty() && menu.getFluidCapacity() > 0) {
            var sprite = RenderHelpers.getAndBindFluidSprite(fluid);
            int fillHeight = (int) Math.ceil(50F * fluid.getAmount() / menu.getFluidCapacity());
            RenderHelpers.fillAreaWithSprite(
                    graphics, sprite, leftPos + 30, topPos + 71 - fillHeight, 16, fillHeight, 16, 16
            );
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.disableBlend();
        }
    }

    @Override protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        if (mouseX >= leftPos + 29 && mouseX < leftPos + 47 && mouseY >= topPos + 20 && mouseY < topPos + 72) {
            graphics.renderTooltip(font, Tooltips.fluidUnitsOf(menu.getFluid()), mouseX, mouseY);
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private static final class BackButton extends AbstractButton {
        private BackButton(int x, int y) {
            super(x, y, 16, 16, Component.translatable("gui.minersdelighttfc.open_cooking"));
        }
        @Override public void onPress() {
            MDTFCNetwork.CHANNEL.sendToServer(new OpenCopperPotMenuC2SPacket());
        }
        @Override protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused()) graphics.fill(getX() - 1, getY() - 1, getX() + 17, getY() + 17, 0x80FFFFFF);
            graphics.renderItem(MDBlocks.COPPER_POT.asStack(), getX(), getY());
            if (isHoveredOrFocused()) {
                graphics.renderTooltip(Minecraft.getInstance().font, getMessage(), mouseX, mouseY);
            }
        }
        @Override protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
