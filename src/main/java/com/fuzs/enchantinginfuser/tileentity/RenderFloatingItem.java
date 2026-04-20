package com.fuzs.enchantinginfuser.client.render;

import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFloatingItem extends TileEntitySpecialRenderer<TileEntityInfuser> {
    
    private final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
    
    @Override
    public void render(TileEntityInfuser te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        ItemStack stack = te.getStackInSlot(0);
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.7, z + 0.5);
            
            float time = (float)(System.currentTimeMillis() % 2000) / 2000.0F;
            float offsetY = (float)Math.sin(Math.PI * time) * 0.05F;
            GlStateManager.translate(0, offsetY, 0);
            
            GlStateManager.rotate((System.currentTimeMillis() % 360) * 0.5F, 0, 1, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            
            IBakedModel model = renderItem.getItemModelWithOverrides(stack, te.getWorld(), null);
            renderItem.renderItem(stack, model);
            
            GlStateManager.popMatrix();
        }
    }
}