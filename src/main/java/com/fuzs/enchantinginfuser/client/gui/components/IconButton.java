package com.fuzs.enchantinginfuser.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class IconButton extends GuiButton {
    
    protected ResourceLocation resourceLocation;
    protected int xTexStart;
    protected int yTexStart;
    protected int yDiffText;
    protected int textureWidth;
    protected int textureHeight;
    private final Runnable onPress;
    
    public IconButton(int buttonId, int x, int y, int width, int height, 
                      int xTexStart, int yTexStart, int yDiffText, 
                      ResourceLocation resourceLocation, 
                      int textureWidth, int textureHeight, 
                      Runnable onPress) {
        super(buttonId, x, y, width, height, "");
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffText = yDiffText;
        this.resourceLocation = resourceLocation;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.onPress = onPress;
    }
    
    public IconButton(int buttonId, int x, int y, int width, int height, 
                      int xTexStart, int yTexStart, int yDiffText, 
                      ResourceLocation resourceLocation, 
                      Runnable onPress) {
        this(buttonId, x, y, width, height, xTexStart, yTexStart, yDiffText, 
             resourceLocation, 256, 256, onPress);
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(this.resourceLocation);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            
            this.hovered = mouseX >= this.x && mouseY >= this.y && 
                           mouseX < this.x + this.width && mouseY < this.y + this.height;
            
            int currentYTex = this.yTexStart;
            
            if (!this.enabled) {
                currentYTex += this.yDiffText * 2;
            } else if (this.hovered) {
                currentYTex += this.yDiffText;
            }
            
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            
            Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, this.xTexStart, currentYTex, this.width, this.height, this.textureWidth, this.textureHeight);
        }
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean isPressed = super.mousePressed(mc, mouseX, mouseY);
        if (isPressed && this.onPress != null) {
            this.onPress.run();
        }
        return isPressed;
    }
}