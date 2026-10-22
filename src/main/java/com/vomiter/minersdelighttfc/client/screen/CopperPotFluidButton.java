package com.vomiter.minersdelighttfc.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotMenu;
import com.vomiter.minersdelighttfc.network.MDTFCNetwork;
import com.vomiter.minersdelighttfc.network.packet.OpenCopperPotFluidMenuC2SPacket;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.OpenPotFluidMenuC2SPacket;
import java.util.ArrayList;
import java.util.List;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public class CopperPotFluidButton extends AbstractButton {
    private final CopperPotMenu menu;
    private final Component basicTooltip;

    public CopperPotFluidButton(int x, int y, Component tooltip, CopperPotMenu menu) {
        super(x, y, 16, 16, Component.empty());
        this.menu = menu;
        this.basicTooltip = tooltip;
    }

    public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(this.basicTooltip);
        FluidStack stack = ((ICookingPotFluidAccess)this.menu.blockEntity).sdtfc$getTank().getFluid();
        if (!stack.isEmpty()) {
            String var10001 = stack.getDisplayName().getString();
            tooltips.add(Component.literal(var10001 + " " + stack.getAmount() + "/4000"));
        }

        if (this.isHoveredOrFocused()) {
            gg.renderComponentTooltip(Minecraft.getInstance().font, tooltips, mouseX, mouseY);
        }

    }

    public void onPress() {
        MDTFCNetwork.CHANNEL.sendToServer(new OpenCopperPotFluidMenuC2SPacket(this.menu.containerId, this.menu.blockEntity.getBlockPos()));
    }

    protected void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (this.isHoveredOrFocused()) {
            gg.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, -2130706433);
        }

        FluidStack stack = ((ICookingPotFluidAccess)this.menu.blockEntity).sdtfc$getTank().getFluid();
        int x = this.getX();
        int y = this.getY() + (this.height - 16) / 2;
        if (!stack.isEmpty()) {
            TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(stack);
            gg.blit(x, y, 0, 16, 16, sprite);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            Item bucket = TFCItems.WOODEN_BUCKET.get();
            gg.renderItem(bucket.getDefaultInstance(), x, y);
        }

    }

    protected void updateWidgetNarration(@NotNull NarrationElementOutput p_259858_) {
    }
}
