package com.fuzs.enchantinginfuser.client.renderer.tileentity;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileEntityInfuserRenderer extends TileEntitySpecialRenderer<TileEntityInfuser> {
    
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(EnchantingInfuserBackport.MODID, "textures/entity/enchanting_infuser_book.png");
    private final ModelBook modelBook = new ModelBook();

    @Override
    public void render(TileEntityInfuser te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null) return;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F, (float)z + 0.5F);
        
        RenderHelper.enableStandardItemLighting(); 
        
        float f = (float)te.tickCount + partialTicks;
        GlStateManager.translate(0.0F, 0.1F + MathHelper.sin(f * 0.1F) * 0.01F, 0.0F);

        float f1;
        for (f1 = te.bookRotation - te.oBookRotation; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) { }
        while (f1 < -(float)Math.PI) { f1 += ((float)Math.PI * 2F); }

        float f2 = te.oBookRotation + f1 * partialTicks;
        GlStateManager.rotate(-f2 * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);
        
        this.bindTexture(BOOK_TEXTURE);
        
        float f3 = te.oPageFlip + (te.pageFlip - te.oPageFlip) * partialTicks;
        float f4 = (float)MathHelper.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = (float)MathHelper.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = te.oBookSpread + (te.bookSpread - te.oBookSpread) * partialTicks;
        
        GlStateManager.enableCull();
        this.modelBook.render(null, f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F), f6, 0.0F, 0.0625F);
        
        RenderHelper.disableStandardItemLighting(); 
        GlStateManager.popMatrix();
    }
}