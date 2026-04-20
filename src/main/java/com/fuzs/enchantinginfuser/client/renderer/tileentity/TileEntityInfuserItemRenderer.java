package com.fuzs.enchantinginfuser.client.renderer.tileentity;

import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class TileEntityInfuserItemRenderer extends TileEntityInfuserRenderer {

    @Override
    public void render(TileEntityInfuser te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        ItemStack itemToEnchant = te.getStackInSlot(0);
        if (!itemToEnchant.isEmpty()) {
            GlStateManager.pushMatrix();
            
            float openness = te.oBookSpread + (te.bookSpread - te.oBookSpread) * partialTicks;
            float hoverOffset = MathHelper.sin((te.tickCount + partialTicks) / 10.0F) * 0.1F + 0.1F;
            GlStateManager.translate((float)x + 0.5F, (float)y + 1.1F + hoverOffset * openness, (float)z + 0.5F);
            
            float rotation = (te.tickCount + partialTicks) * 2.0F;
            GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
            

            float scale = openness * 0.8F + 0.2F;
            GlStateManager.scale(scale, scale, scale);

            RenderHelper.enableStandardItemLighting();
            
            Minecraft.getMinecraft().getRenderItem().renderItem(itemToEnchant, ItemCameraTransforms.TransformType.GROUND);
            
            RenderHelper.disableStandardItemLighting();
            
            GlStateManager.popMatrix();
        }
    }
}