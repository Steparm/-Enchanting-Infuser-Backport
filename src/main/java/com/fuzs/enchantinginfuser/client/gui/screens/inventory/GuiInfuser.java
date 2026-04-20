package com.fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import com.fuzs.enchantinginfuser.client.gui.components.IconButton;
import com.fuzs.enchantinginfuser.network.message.MessageEnchant;
import com.fuzs.enchantinginfuser.network.message.MessageUpdateEnchantment;
import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;
import com.fuzs.enchantinginfuser.world.inventory.ContainerInfuser;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiInfuser extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(EnchantingInfuserBackport.MODID, "textures/gui/container/enchanting_infuser.png");
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("minecraft:textures/gui/widgets.png");
    
    private final ContainerInfuser container;
    private final TileEntityInfuser tileEntity;
    private final boolean isAdvanced;
    
    private GuiTextField searchBox;
    private ScrollingList scrollingList;
    private IconButton enchantButton;
    private IconButton repairButton;
    
    private float scrollOffs;
    private boolean isScrolling;
    private ItemStack lastStack = ItemStack.EMPTY;
    private Random enchantmentRandom = new Random();
    
    private Map<Enchantment, Integer> cachedEnchantmentLevels = new HashMap<>();
    
    private static final String SGA_CHARS = "abcdefghijklmnopqrstuvwxyz";

    public GuiInfuser(InventoryPlayer playerInv, World world, BlockPos pos, boolean isAdvanced) {
        super(new ContainerInfuser(playerInv, world, pos, isAdvanced));
        this.container = (ContainerInfuser) this.inventorySlots;
        this.tileEntity = container.tileEntity;
        this.isAdvanced = isAdvanced;
        this.xSize = 220;
        this.ySize = 185;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.searchBox = new GuiTextField(0, this.fontRenderer, this.guiLeft + 67, this.guiTop + 6, 116, 9);
        this.searchBox.setEnableBackgroundDrawing(false);
        this.searchBox.setMaxStringLength(30);
        this.searchBox.setTextColor(0xFFFFFF);

        this.scrollingList = new ScrollingList(this.guiLeft + 28, this.guiTop + 19, 162, 18, 4);

        int enchantBtnY = isAdvanced ? 44 : 55;
        this.enchantButton = new IconButton(0, this.guiLeft + 7, this.guiTop + enchantBtnY, 18, 18, 126, 185, 18, GUI_TEXTURE, () -> {
            if (tileEntity.canEnchant(mc.player)) {
                if (mc.isIntegratedServerRunning()) {
                    EnchantingInfuserBackport.NETWORK.sendToServer(new MessageEnchant());
                } else {
                    tileEntity.enchantItem(mc.player);
                }
                refreshSearchResults();
            }
        });
        this.buttonList.add(this.enchantButton);
        
        if (isAdvanced) {
            this.repairButton = new IconButton(1, this.guiLeft + 7, this.guiTop + 66, 18, 18, 144, 185, 18, GUI_TEXTURE, () -> {
                tileEntity.repairItem(mc.player);
                refreshSearchResults();
            });
            this.buttonList.add(this.repairButton);
        }

        refreshSearchResults();
        lastStack = tileEntity.getStackInSlot(0).copy();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.searchBox.updateCursorCounter();
        
        ItemStack currentStack = tileEntity.getStackInSlot(0);
        
        if (!ItemStack.areItemStacksEqual(currentStack, lastStack)) {
            lastStack = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
            
            if (!currentStack.isEmpty()) {
                refreshSearchResults();
            }
            return;
        }
        
        if (!currentStack.isEmpty()) {
            boolean levelsChanged = false;
            for (Map.Entry<Enchantment, Integer> entry : getSortedEntries()) {
                Integer cachedLevel = cachedEnchantmentLevels.get(entry.getKey());
                if (cachedLevel == null || !cachedLevel.equals(entry.getValue())) {
                    levelsChanged = true;
                    break;
                }
            }
            
            if (levelsChanged) {
                updateEnchantmentLevels();
            }
        }
        
        this.enchantButton.enabled = tileEntity.canEnchant(mc.player);
        if (this.repairButton != null) {
            this.repairButton.enabled = tileEntity.canRepair(mc.player);
        }
    }
    
    private List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        List<Map.Entry<Enchantment, Integer>> list = new ArrayList<>(tileEntity.selectedEnchantments.entrySet());
        list.sort(Comparator.comparing(e -> e.getKey().getName()));
        return list;
    }
    
    private void updateEnchantmentLevels() {
        cachedEnchantmentLevels.clear();
        for (Map.Entry<Enchantment, Integer> entry : getSortedEntries()) {
            cachedEnchantmentLevels.put(entry.getKey(), entry.getValue());
        }
        
        for (EnchantmentEntry entry : scrollingList.entries) {
            Integer newLevel = cachedEnchantmentLevels.get(entry.enchantment);
            if (newLevel != null) {
                entry.setLevel(newLevel);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        
        if (tileEntity.getStackInSlot(0).isEmpty()) {
            tileEntity.selectedEnchantments.clear();
            tileEntity.enchantmentsChanged = false;
            tileEntity.enchantingCost = 0;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.searchBox.textboxKeyTyped(typedChar, keyCode)) {
            refreshSearchResults();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchBox.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (mouseX >= guiLeft + 197 && mouseX < guiLeft + 211 && mouseY >= guiTop + 17 && mouseY < guiTop + 89) {
            this.isScrolling = this.scrollingList.canScroll() && !tileEntity.getStackInSlot(0).isEmpty();
            return;
        }
        
        if (mouseX >= guiLeft + 28 && mouseX <= guiLeft + 190) {
            this.scrollingList.handleMouseClick(mouseX, mouseY, mouseButton);
        }
    }
    
    @Override
    protected void mouseReleased(int mx, int my, int state) {
        if (state == 0) this.isScrolling = false;
        super.mouseReleased(mx, my, state);
    }
    
    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (this.isScrolling) {
            this.scrollOffs = Math.max(0, Math.min(1, (float)(my - this.guiTop - 17 - 7.5) / 57f));
            this.scrollingList.scrollTo(this.scrollOffs);
        }
        super.mouseClickMove(mx, my, btn, time);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && !tileEntity.getStackInSlot(0).isEmpty() && this.scrollingList.canScroll()) {
            this.scrollOffs = Math.max(0, Math.min(1, this.scrollOffs - (float) wheel / 120f / (this.scrollingList.getItemCount() - 4)));
            this.scrollingList.scrollTo(this.scrollOffs);
        }
    }

    public void refreshSearchResults() {
        if (tileEntity.getStackInSlot(0).isEmpty()) {
            return;
        }
        
        this.scrollingList.clearEntries();
        cachedEnchantmentLevels.clear();
        String filter = searchBox.getText().toLowerCase().trim();
        
        for (Map.Entry<Enchantment, Integer> entry : getSortedEntries()) {
            String name = I18n.format(entry.getKey().getName()).toLowerCase();
            if (filter.isEmpty() || name.contains(filter)) {
                this.scrollingList.addEntry(new EnchantmentEntry(entry.getKey(), entry.getValue()));
                cachedEnchantmentLevels.put(entry.getKey(), entry.getValue());
            }
        }
        this.scrollOffs = 0;
        this.scrollingList.scrollTo(0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = this.guiLeft;
        int j = this.guiTop;

        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        this.searchBox.drawTextBox();
        this.drawTexturedModalRect(i + 7, j + 30, 162, 185, 18, 18);

        if (isAdvanced) {
            this.drawTexturedModalRect(i + 8, j + 103, 162, 185, 18, 18);
            this.mc.getTextureManager().bindTexture(WIDGETS_TEXTURE);
            this.drawTexturedModalRect(i + 8, j + 103, 162, 185, 18, 18);
            
            this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
            this.drawTexturedModalRect(i + 8, j + 121, 162, 185, 18, 18);
            this.mc.getTextureManager().bindTexture(WIDGETS_TEXTURE);
            this.drawTexturedModalRect(i + 10, j + 123, 16, 168, 14, 14);
            
            this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
            this.drawTexturedModalRect(i + 196, j + 103, 162, 185, 18, 18);
            this.mc.getTextureManager().bindTexture(WIDGETS_TEXTURE);
            this.drawTexturedModalRect(i + 198, j + 105, 32, 168, 14, 14);
            
            this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
            this.drawTexturedModalRect(i + 196, j + 121, 162, 185, 18, 18);
            this.mc.getTextureManager().bindTexture(WIDGETS_TEXTURE);
            this.drawTexturedModalRect(i + 198, j + 123, 48, 168, 14, 14);
        }

        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(i + 8, j + 160, 163, 185, 18, 18);

        int k = (int)(58.0F * this.scrollOffs);
        this.drawTexturedModalRect(i + 197, j + 14 + k, 222, 74, 18, 18);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format(isAdvanced ? "container.advanced_enchanting_infuser" : "container.enchanting_infuser");
        this.fontRenderer.drawString(title, 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 30, this.ySize - 94, 4210752);

        if (!tileEntity.getStackInSlot(0).isEmpty() && scrollingList.entries.size() > 0) {
            this.scrollingList.draw(mouseX - guiLeft, mouseY - guiTop);
        }

        int enchantBtnY = isAdvanced ? 44 : 55;
        int enchantCost = tileEntity.enchantingCost;
        if (enchantCost > 0) {
            String s = String.valueOf(enchantCost);
            int x = 7 + 19 - fontRenderer.getStringWidth(s);
            int y = enchantBtnY + 11;
            fontRenderer.drawString(s, x + 1, y, 0, false);
            fontRenderer.drawString(s, x - 1, y, 0, false);
            fontRenderer.drawString(s, x, y + 1, 0, false);
            fontRenderer.drawString(s, x, y - 1, 0, false);
            fontRenderer.drawString(s, x, y, 0x80FF20, false);
        }

        if (repairButton != null) {
            int repairCost = tileEntity.repairCost;
            if (repairCost > 0) {
                String s = String.valueOf(repairCost);
                int x = 7 + 19 - fontRenderer.getStringWidth(s);
                int y = 66 + 11;
                fontRenderer.drawString(s, x + 1, y, 0, false);
                fontRenderer.drawString(s, x - 1, y, 0, false);
                fontRenderer.drawString(s, x, y + 1, 0, false);
                fontRenderer.drawString(s, x, y - 1, 0, false);
                fontRenderer.drawString(s, x, y, 0xFFFFFF, false);
            }
        }

        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(Blocks.BOOKSHELF), 196, 159);
        RenderHelper.disableStandardItemLighting();

        int currentPower = Math.min(tileEntity.enchantingPower, 15);
        int maxPower = 15;
        String powerStr = currentPower + "/" + maxPower;
        int powerColor = currentPower >= maxPower ? 0x55FF55 : 0xFF5555;
        
        int powerX = 213 - fontRenderer.getStringWidth(powerStr);
        int powerY = 174;
        
        fontRenderer.drawString(powerStr, powerX + 1, powerY, 0x000000, false);
        fontRenderer.drawString(powerStr, powerX - 1, powerY, 0x000000, false);
        fontRenderer.drawString(powerStr, powerX, powerY + 1, 0x000000, false);
        fontRenderer.drawString(powerStr, powerX, powerY - 1, 0x000000, false);
        fontRenderer.drawString(powerStr, powerX, powerY, powerColor, false);
    }

    private class ScrollingList {
        private final List<EnchantmentEntry> entries = new ArrayList<>();
        private final int x, y, w, h, len;
        private int scrollPos;
        
        public ScrollingList(int x, int y, int w, int h, int len) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.len = len;
        }
        
        public void scrollTo(float pos) {
            if (canScroll()) {
                this.scrollPos = Math.round((entries.size() - len) * pos);
                this.scrollPos = Math.max(0, Math.min(scrollPos, entries.size() - len));
            } else {
                this.scrollPos = 0;
            }
        }
        
        public boolean canScroll() {
            return entries.size() > len;
        }
        
        public void clearEntries() {
            entries.clear();
        }
        
        public void addEntry(EnchantmentEntry entry) {
            entries.add(entry);
        }
        
        public int getItemCount() {
            return entries.size();
        }
        
        public void draw(int mouseX, int mouseY) {
            for (int i = 0; i < Math.min(len, entries.size()); i++) {
                entries.get(scrollPos + i).draw(x - guiLeft, y + h * i - guiTop, w, h, mouseX, mouseY);
            }
        }
        
        public void handleMouseClick(int mouseX, int mouseY, int button) {
            if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h * len) {
                int index = scrollPos + (mouseY - y) / h;
                if (index >= 0 && index < entries.size()) {
                    entries.get(index).onClick(mouseX, mouseY, button);
                }
            }
        }
    }

    private class EnchantmentEntry {
        private final Enchantment enchantment;
        private int level;
        private IconButton decrBtn, incrBtn;
        private int obfuscatedSeed;
        
        public EnchantmentEntry(Enchantment ench, int lvl) {
            this.enchantment = ench;
            this.level = lvl;
            this.obfuscatedSeed = enchantmentRandom.nextInt();
            
            this.decrBtn = new IconButton(0, 0, 0, 18, 18, 220, 0, 18, GUI_TEXTURE, () -> {
                int newLevel = level - 1;
                if (mc.isIntegratedServerRunning()) {
                    EnchantingInfuserBackport.NETWORK.sendToServer(new MessageUpdateEnchantment(enchantment, newLevel));
                }
                tileEntity.updateEnchantment(enchantment, newLevel);
                this.level = newLevel;
                refreshSearchResults();
            });
            
            this.incrBtn = new IconButton(0, 0, 0, 18, 18, 238, 0, 18, GUI_TEXTURE, () -> {
                int newLevel = level + 1;
                if (mc.isIntegratedServerRunning()) {
                    EnchantingInfuserBackport.NETWORK.sendToServer(new MessageUpdateEnchantment(enchantment, newLevel));
                }
                tileEntity.updateEnchantment(enchantment, newLevel);
                this.level = newLevel;
                refreshSearchResults();
            });
        }
        
        public void setLevel(int newLevel) {
            this.level = newLevel;
        }
        
        public void draw(int x, int y, int w, int h, int mouseX, int mouseY) {
            int maxAllowedLevel = tileEntity.getMaxLevelForEnchantment(enchantment);
            boolean available = level > 0 || tileEntity.enchantingPower >= 1;
            boolean canIncrease = level < enchantment.getMaxLevel() && level < maxAllowedLevel;
            boolean canDecrease = level > 0;
            boolean compatible = tileEntity.isEnchantmentCompatible(enchantment, level);
            boolean obfuscated = maxAllowedLevel == 0;
            
            if (!available || !compatible || (!canIncrease && level == 0) || obfuscated) {
                GlStateManager.color(0.5F, 0.45F, 0.35F, 1.0F);
            } else {
                GlStateManager.color(1, 1, 1, 1);
            }
            
            mc.getTextureManager().bindTexture(GUI_TEXTURE);
            drawTexturedModalRect(x + 18, y, 0, 203, 126, 18);
            
            GlStateManager.color(1, 1, 1, 1);
            
            String displayName;
            FontRenderer fr = fontRenderer;
            if (obfuscated) {
                fr = mc.standardGalacticFontRenderer;
                Random rand = new Random(this.obfuscatedSeed);
                StringBuilder sb = new StringBuilder();
                int length = 12 + rand.nextInt(8);
                for (int i = 0; i < length; i++) {
                    sb.append(SGA_CHARS.charAt(rand.nextInt(SGA_CHARS.length())));
                }
                displayName = sb.toString();
            } else {
                displayName = I18n.format(enchantment.getName());
                if (level > 0) {
                    displayName = displayName + " " + I18n.format("enchantment.level." + level);
                }
            }
            
            int textColor = (available && compatible && (level > 0 || canIncrease) && !obfuscated) ? 0xFFFFFF : 0x685E4A;
            
            int textX = x + w / 2 - fr.getStringWidth(displayName) / 2;
            fr.drawString(displayName, textX, y + 5, textColor);
            
            decrBtn.x = x + guiLeft;
            decrBtn.y = y + guiTop;
            decrBtn.visible = canDecrease && !obfuscated;
            decrBtn.enabled = canDecrease && !obfuscated;
            
            incrBtn.x = x + w - 18 + guiLeft;
            incrBtn.y = y + guiTop;
            incrBtn.visible = canIncrease && !obfuscated;
            incrBtn.enabled = canIncrease && !obfuscated;
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(-guiLeft, -guiTop, 0);
            decrBtn.drawButton(mc, mouseX + guiLeft, mouseY + guiTop, 0);
            incrBtn.drawButton(mc, mouseX + guiLeft, mouseY + guiTop, 0);
            GlStateManager.popMatrix();
        }
        
        public void onClick(int mx, int my, int mb) {
            int decrX = decrBtn.x;
            int decrY = decrBtn.y;
            if (mx >= decrX && mx < decrX + 18 && my >= decrY && my < decrY + 18) {
                decrBtn.mousePressed(mc, mx, my);
                decrBtn.playPressSound(mc.getSoundHandler());
            }
            int incrX = incrBtn.x;
            int incrY = incrBtn.y;
            if (mx >= incrX && mx < incrX + 18 && my >= incrY && my < incrY + 18) {
                incrBtn.mousePressed(mc, mx, my);
                incrBtn.playPressSound(mc.getSoundHandler());
            }
        }
    }
}