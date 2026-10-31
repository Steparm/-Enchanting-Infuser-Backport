package com.fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import com.fuzs.enchantinginfuser.client.gui.components.IconButton;
import com.fuzs.enchantinginfuser.config.ModConfig;
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
import net.minecraft.util.text.TextFormatting;
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

        this.enchantButton = new IconButton(0, this.guiLeft + 7, this.guiTop + 55, 18, 18, 126, 203, 18, GUI_TEXTURE, () -> {
            if (tileEntity.canEnchant(mc.player)) {
                EnchantingInfuserBackport.NETWORK.sendToServer(new MessageEnchant(false));
                refreshSearchResults();
            }
        });
        this.buttonList.add(this.enchantButton);
        
        if (isAdvanced) {
            this.repairButton = new IconButton(1, this.guiLeft + 7, this.guiTop + 74, 18, 18, 144, 203, 18, GUI_TEXTURE, () -> {
                if (tileEntity.canRepair(mc.player)) {
                    EnchantingInfuserBackport.NETWORK.sendToServer(new MessageEnchant(true));
                    refreshSearchResults();
                }
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
        
        ItemStack stack = tileEntity.getStackInSlot(0);
        
        if (stack.isEmpty()) {
            this.enchantButton.enabled = false;
        } else if (!tileEntity.enchantmentsChanged) {
            this.enchantButton.enabled = true;
        } else {
            boolean hasEnoughExp = mc.player.experienceLevel >= tileEntity.enchantingCost || mc.player.capabilities.isCreativeMode;
            this.enchantButton.enabled = hasEnoughExp;
        }
        
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
            Enchantment ench = entry.getKey();
            
            if (ModConfig.isEnchantmentBlacklisted(ench, this.isAdvanced)) {
                continue;
            }
            
            String name = I18n.format(ench.getName()).toLowerCase();
            if (filter.isEmpty() || name.contains(filter)) {
                this.scrollingList.addEntry(new EnchantmentEntry(ench, entry.getValue()));
                cachedEnchantmentLevels.put(ench, entry.getValue());
            }
        }
        
        if (this.scrollingList.canScroll()) {
            this.scrollingList.scrollPos = Math.min(this.scrollingList.scrollPos,
                Math.max(0, this.scrollingList.entries.size() - 4));
        } else {
            this.scrollingList.scrollPos = 0;
        }
        this.scrollingList.scrollTo(this.scrollOffs);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        this.drawEnchantmentTooltip(mouseX, mouseY);
    }

    private void drawEnchantmentTooltip(int mouseX, int mouseY) {
        if (tileEntity.getStackInSlot(0).isEmpty() || scrollingList.entries.isEmpty()) {
            return;
        }
        
        int guiMouseX = mouseX - guiLeft;
        int guiMouseY = mouseY - guiTop;
        
        if (guiMouseX < 28 || guiMouseX > 28 + 162) {
            return;
        }
        if (guiMouseY < 19 || guiMouseY > 19 + 18 * 4) {
            return;
        }
        
        int index = scrollingList.scrollPos + (guiMouseY - 19) / 18;
        if (index < 0 || index >= scrollingList.entries.size()) {
            return;
        }
        
        EnchantmentEntry entry = scrollingList.entries.get(index);
        Enchantment enchantment = entry.enchantment;
        
        int entryX = 28 + 18;
        int entryWidth = 162 - 36;
        if (guiMouseX < entryX || guiMouseX > entryX + entryWidth) {
            return;
        }
        
        List<String> tooltip = new ArrayList<>();
        
        String displayName = I18n.format(enchantment.getName());
        if (entry.level > 0) {
            displayName += " " + I18n.format("enchantment.level." + entry.level);
        }
        tooltip.add(TextFormatting.AQUA + displayName);
        
        tooltip.add(TextFormatting.GRAY + I18n.format("gui.enchantinginfuser.max_level", enchantment.getMaxLevel()));
        
        String description = getEnchantmentDescription(enchantment);
        if (description != null && !description.isEmpty()) {
            tooltip.add("");
            for (String line : wrapText(description, 200)) {
                tooltip.add(TextFormatting.WHITE + line);
            }
        }
        
        int originalLevel = tileEntity.originalEnchantments.getOrDefault(enchantment, 0);
        if (entry.level > originalLevel) {
            int levelsAdded = entry.level - originalLevel;
            int cost = levelsAdded * getRarityCostForTooltip(enchantment.getRarity());
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + I18n.format("gui.enchantinginfuser.cost", cost));
        }
        
        this.drawHoveringText(tooltip, mouseX, mouseY);
    }

    private String getEnchantmentDescription(Enchantment enchantment) {
        ResourceLocation registryName = enchantment.getRegistryName();
        if (registryName == null) {
            return null;
        }
        
        String fullName = registryName.toString();
        int colonIdx = fullName.indexOf(':');
        String domain = colonIdx >= 0 ? fullName.substring(0, colonIdx) : "minecraft";
        String path = colonIdx >= 0 ? fullName.substring(colonIdx + 1) : fullName;
        
        String descKey = "enchantment." + domain + "." + path + ".desc";
        String desc = I18n.format(descKey);
        
        if (desc.equals(descKey)) {
            return null;
        }
        return desc;
    }

    private int getRarityCostForTooltip(Enchantment.Rarity rarity) {
        switch (rarity) {
            case COMMON: return 1;
            case UNCOMMON: return 2;
            case RARE: return 4;
            case VERY_RARE: return 8;
            default: return 1;
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (this.fontRenderer.getStringWidth(testLine) > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = this.guiLeft;
        int j = this.guiTop;

        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        
        this.searchBox.drawTextBox();
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);

        this.drawTexturedModalRect(i + 7, j + 30, 162, 185, 18, 18);

        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(i + 8, j + 160, 163, 185, 18, 18);

        boolean hasItem = !tileEntity.getStackInSlot(0).isEmpty();
        
        if (hasItem) {
            if (scrollingList.canScroll()) {
                int k = (int)(57.0F * this.scrollOffs);
                this.drawTexturedModalRect(i + 198, j + 16 + k, 220, 72, 18, 18);
            } else {
                this.drawTexturedModalRect(i + 198, j + 16, 220, 54, 18, 18);
            }
        } else {
            if (scrollingList.canScroll()) {
                int k = (int)(57.0F * this.scrollOffs);
                this.drawTexturedModalRect(i + 198, j + 16 + k, 220, 54, 18, 18);
            } else {
                this.drawTexturedModalRect(i + 198, j + 16, 220, 54, 18, 18);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format(isAdvanced ? "container.advanced_enchanting_infuser" : "container.enchanting_infuser");
        
        if (this.fontRenderer.getStringWidth(title) > 55) {
            title = this.fontRenderer.trimStringToWidth(title, 52) + "...";
        }
        this.fontRenderer.drawString(title, 8, 6, 4210752);
        
        this.fontRenderer.drawString(I18n.format("container.inventory"), 30, this.ySize - 94, 4210752);

        if (!tileEntity.getStackInSlot(0).isEmpty() && scrollingList.entries.size() > 0) {
            this.scrollingList.draw(mouseX - guiLeft, mouseY - guiTop);
        }

        int enchantCost = tileEntity.enchantingCost;
        if (enchantCost > 0) {
            String s = String.valueOf(enchantCost);
            int x = 7 + 19 - fontRenderer.getStringWidth(s);
            int y = 55 + 11;
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
                int y = 74 + 11;
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

        String powerStr = String.valueOf(tileEntity.enchantingPower);
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.translate(0, 0, 500.0F); 
        fontRenderer.drawStringWithShadow(powerStr, 212 - fontRenderer.getStringWidth(powerStr), 170, 0xFFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private class ScrollingList {
        public final List<EnchantmentEntry> entries = new ArrayList<>();
        private final int x, y, w, h, len;
        public int scrollPos;
        
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
            int maxScroll = Math.max(0, entries.size() - len);
            int actualScroll = Math.max(0, Math.min(scrollPos, maxScroll));
            
            for (int i = 0; i < Math.min(len, entries.size()); i++) {
                int idx = actualScroll + i;
                if (idx >= 0 && idx < entries.size()) {
                    entries.get(idx).draw(x - guiLeft, y + h * i - guiTop, w, h, mouseX, mouseY);
                }
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
        public final Enchantment enchantment;
        public int level;
        private IconButton decrBtn, incrBtn;
        private int obfuscatedSeed;
        
        public EnchantmentEntry(Enchantment ench, int lvl) {
            this.enchantment = ench;
            this.level = lvl;
            this.obfuscatedSeed = enchantmentRandom.nextInt();
            
            this.decrBtn = new IconButton(0, 0, 0, 18, 18, 220, 18, 18, GUI_TEXTURE, () -> {
                int newLevel = level - 1;
                EnchantingInfuserBackport.NETWORK.sendToServer(new MessageUpdateEnchantment(enchantment, newLevel));
                tileEntity.updateEnchantment(enchantment, newLevel);
                this.level = newLevel;
                float oldScroll = scrollOffs;
                refreshSearchResults();
                scrollOffs = oldScroll;
                scrollingList.scrollTo(scrollOffs);
            });
            
            this.incrBtn = new IconButton(0, 0, 0, 18, 18, 238, 18, 18, GUI_TEXTURE, () -> {
                int newLevel = level + 1;
                EnchantingInfuserBackport.NETWORK.sendToServer(new MessageUpdateEnchantment(enchantment, newLevel));
                tileEntity.updateEnchantment(enchantment, newLevel);
                this.level = newLevel;
                float oldScroll = scrollOffs;
                refreshSearchResults();
                scrollOffs = oldScroll;
                scrollingList.scrollTo(scrollOffs);
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
            boolean isSelected = tileEntity.selectedEnchantments.containsKey(enchantment) && tileEntity.selectedEnchantments.get(enchantment) == level && level > 0;
            
            if (!available || !compatible || (!canIncrease && level == 0) || obfuscated) {
                GlStateManager.color(0.5F, 0.45F, 0.35F, 1.0F);
            } else {
                GlStateManager.color(1, 1, 1, 1);
            }
            
            mc.getTextureManager().bindTexture(GUI_TEXTURE);
            
            int textureY = isSelected ? 221 : 203;
            drawTexturedModalRect(x + 18, y, 0, textureY, 126, 18);
            
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
            decrBtn.visible = true;
            decrBtn.enabled = canDecrease && compatible;
            
            incrBtn.x = x + w - 18 + guiLeft;
            incrBtn.y = y + guiTop;
            incrBtn.visible = true;
            incrBtn.enabled = canIncrease && compatible;
            
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
                if (decrBtn.visible && decrBtn.enabled) {
                    decrBtn.mousePressed(mc, mx, my);
                    decrBtn.playPressSound(mc.getSoundHandler());
                }
            }
            int incrX = incrBtn.x;
            int incrY = incrBtn.y;
            if (mx >= incrX && mx < incrX + 18 && my >= incrY && my < incrY + 18) {
                if (incrBtn.visible && incrBtn.enabled) {
                    incrBtn.mousePressed(mc, mx, my);
                    incrBtn.playPressSound(mc.getSoundHandler());
                }
            }
        }
    }
}