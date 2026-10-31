package com.fuzs.enchantinginfuser.tileentity;

import com.fuzs.enchantinginfuser.config.ModConfig;
import com.fuzs.enchantinginfuser.world.inventory.ContainerInfuser;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;

public class TileEntityInfuser extends TileEntity implements ITickable {
    
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    
    public int tickCount;
    public float bookRotation;
    public float oBookRotation;
    public float pageFlip;
    public float oPageFlip;
    public float bookSpread;
    public float oBookSpread;
    public float flipRandom;
    public float flipRotation;
    
    private final Random rand = new Random();
    
    public Map<Enchantment, Integer> selectedEnchantments = new HashMap<>();
    public Map<Enchantment, Integer> originalEnchantments = new HashMap<>();
    public int enchantingCost = 0;
    public int enchantingPower = 0;
    public int repairCost = 0;
    public boolean enchantmentsChanged = false;
    public ItemStack originalStack = ItemStack.EMPTY;
    
    private boolean advanced = false;
    
    public TileEntityInfuser() {
        this.tickCount = 0;
        this.bookRotation = 0;
        this.oBookRotation = 0;
        this.pageFlip = 0;
        this.oPageFlip = 0;
        this.bookSpread = 0;
        this.oBookSpread = 0;
        this.flipRandom = 0;
        this.flipRotation = 0;
    }
    
    public boolean isAdvanced() {
        return this.advanced;
    }
    
    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }
    
    @Override
    public void update() {
        this.oBookRotation = this.bookRotation;
        this.oPageFlip = this.pageFlip;
        this.oBookSpread = this.bookSpread;
        this.tickCount++;
        
        if (this.tickCount % 20 == 0) {
            this.updatePower();
        }
        
        if (world.isRemote) {
            EntityPlayer player = world.getClosestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false
            );
            
            if (player != null) {
                double dx = player.posX - (pos.getX() + 0.5);
                double dz = player.posZ - (pos.getZ() + 0.5);
                float targetRot = (float) Math.atan2(dz, dx);
                
                float diff = targetRot - this.bookRotation;
                while (diff >= (float)Math.PI) diff -= (float)Math.PI * 2;
                while (diff < -(float)Math.PI) diff += (float)Math.PI * 2;
                this.bookRotation += diff * 0.1f;
                
                this.bookSpread += 0.1f;
                if (this.bookSpread > 1.0f) this.bookSpread = 1.0f;
            } else {
                this.bookRotation += 0.02f;
                if (this.bookRotation > (float)Math.PI * 2) {
                    this.bookRotation -= (float)Math.PI * 2;
                }
                
                this.bookSpread -= 0.05f;
                if (this.bookSpread < 0.0f) this.bookSpread = 0.0f;
            }
            
            if (this.bookSpread > 0.0f) {
                if (this.flipRandom <= 0.0f) {
                    this.flipRandom = (float) (rand.nextInt(40) + 20);
                    this.flipRotation = 0.0f;
                } else {
                    this.flipRandom -= 1.0f;
                    this.flipRotation += 0.1f;
                }
                
                if (this.flipRotation > 1.0f) {
                    this.flipRotation = 1.0f;
                }
                
                this.pageFlip = this.flipRotation;
            } else {
                this.pageFlip = 0.0f;
                this.flipRandom = 0.0f;
                this.flipRotation = 0.0f;
            }
        }
    }
    
    public ItemStackHandler getInventory() {
        return inventory;
    }
    
    public ItemStackHandler getInventoryHandler() {
        return inventory;
    }
    
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }
    
    public void setStackInSlot(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }
    
    public void onItemUpdated() {
        ItemStack currentStack = getStackInSlot(0);
        
        if (!currentStack.isEmpty()) {
            if (originalStack.isEmpty() || !ItemStack.areItemsEqual(currentStack, originalStack)) {
                originalStack = currentStack.copy();
                enchantmentsChanged = false;
                loadEnchantmentsFromItem();
            }
        } else {
            originalStack = ItemStack.EMPTY;
            selectedEnchantments.clear();
            enchantmentsChanged = false;
            enchantingCost = 0;
            repairCost = 0;
        }
        
        updatePower();
    }
    
    private void loadEnchantmentsFromItem() {
        ItemStack stack = getStackInSlot(0);
        if (!stack.isEmpty()) {
            selectedEnchantments.clear();
            Map<Enchantment, Integer> existing = EnchantmentHelper.getEnchantments(stack);
            for (Enchantment ench : Enchantment.REGISTRY) {
                if (ench != null && ench.canApply(stack)) {
                    if (ModConfig.isEnchantmentBlacklisted(ench, this.advanced)) {
                        continue;
                    }
                    int level = existing.getOrDefault(ench, 0);
                    selectedEnchantments.put(ench, level);
                }
            }
            this.originalEnchantments = new HashMap<>(selectedEnchantments);
            calculateCost();
            this.repairCost = calculateRepairCost();
        }
    }
    
    private int calculateRepairCost() {
        ItemStack stack = getStackInSlot(0);
        if (stack.isEmpty() || !stack.isItemDamaged()) return 0;
        
        int damage = stack.getItemDamage();
        int maxDamage = stack.getMaxDamage();
        double damageRatio = (double) damage / maxDamage;
        
        int cost = (int) Math.ceil(damageRatio * 30);
        return Math.max(1, cost);
    }
    
    public void updateEnchantment(Enchantment ench, int newLevel) {
        if (getStackInSlot(0).isEmpty()) return;
        if (ModConfig.isEnchantmentBlacklisted(ench, this.advanced)) return;
        
        int maxAllowedLevel = getMaxLevelForEnchantment(ench);
        if (newLevel >= 0 && newLevel <= ench.getMaxLevel() && newLevel <= maxAllowedLevel) {
            if (newLevel > 0) {
                for (Map.Entry<Enchantment, Integer> entry : selectedEnchantments.entrySet()) {
                    if (entry.getValue() > 0 && entry.getKey() != ench) {
                        if (!entry.getKey().isCompatibleWith(ench)) {
                            return;
                        }
                    }
                }
            }
            
            selectedEnchantments.put(ench, newLevel);
            this.enchantmentsChanged = !selectedEnchantments.equals(originalEnchantments);
            calculateCost();
            markDirty();
        }
    }
    
    public void enchantItem(EntityPlayer player) {
        if (!canEnchant(player)) return;
        
        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-enchantingCost);
        }
        
        Map<Enchantment, Integer> toApply = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : selectedEnchantments.entrySet()) {
            if (entry.getValue() > 0 && !ModConfig.isEnchantmentBlacklisted(entry.getKey(), this.advanced)) {
                toApply.put(entry.getKey(), entry.getValue());
            }
        }
        
        ItemStack newStack = originalStack.copy();
        if (newStack.getItem() == Items.BOOK && !toApply.isEmpty()) {
            newStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        EnchantmentHelper.setEnchantments(toApply, newStack);
        
        inventory.setStackInSlot(0, newStack);
        markDirty();
        
        originalStack = newStack.copy();
        this.originalEnchantments = new HashMap<>(selectedEnchantments);
        this.enchantmentsChanged = false;
        calculateCost();
        
        if (!world.isRemote && player.openContainer instanceof ContainerInfuser) {
            ((ContainerInfuser) player.openContainer).detectAndSendChanges();
        }
    }
    
    public void repairItem(EntityPlayer player) {
        if (!canRepair(player)) return;
        
        ItemStack stack = getStackInSlot(0);
        
        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-repairCost);
        }
        
        ItemStack newStack = stack.copy();
        newStack.setItemDamage(0);
        inventory.setStackInSlot(0, newStack);
        markDirty();
        
        originalStack = newStack.copy();
        enchantmentsChanged = false;
        loadEnchantmentsFromItem();
        
        if (!world.isRemote && player.openContainer instanceof ContainerInfuser) {
            ContainerInfuser container = (ContainerInfuser) player.openContainer;
            container.getSlot(0).onSlotChanged();
            container.detectAndSendChanges();
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).sendContainerToPlayer(container);
            }
        }
    }
    
    private void updatePower() {
        int newPower = getEnchantingPower();
        if (newPower != this.enchantingPower) {
            this.enchantingPower = newPower;
            markDirty();
        }
    }
    
    public int getEnchantingPower() {
        int power = 0;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -2; k <= 2; ++k) {
                    if (i == 0 && k == 0) continue;
                    BlockPos checkPos = pos.add(i, j, k);
                    if (world != null) {
                        Block block = world.getBlockState(checkPos).getBlock();
                        if (ModConfig.validBookshelves.contains(block)) {
                            power++;
                        }
                    }
                }
            }
        }
        return Math.min(power, 15);
    }
    
    public int getMaxLevelForEnchantment(Enchantment enchantment) {
        int currentPower = enchantingPower;
        int maxPower = 15;
        if (currentPower < getMinPowerByRarity(enchantment, maxPower)) return 0;
        int totalLevels = enchantment.getMaxLevel();
        double levelRange = maxPower * 0.6;
        double levelPercentile = totalLevels > 0 ? levelRange / totalLevels : 0;
        for (int i = 0; i <= totalLevels; i++) {
            int nextPower = Math.min(maxPower, (int)Math.ceil(getMinPowerByRarity(enchantment, maxPower) + i * levelPercentile));
            if (currentPower < nextPower) return i;
        }
        return enchantment.getMaxLevel();
    }
    
    private int getMinPowerByRarity(Enchantment enchantment, int maxPower) {
        int rarityOrdinal = enchantment.getRarity().ordinal();
        double multiplier;
        switch (rarityOrdinal) {
            case 0: multiplier = 0.15; break;
            case 1: multiplier = 0.3; break;
            case 2: multiplier = 0.45; break;
            default: multiplier = 0.6; break;
        }
        return (int)Math.round(maxPower * multiplier);
    }
    
    private void calculateCost() {
        int cost = 0;
        for (Map.Entry<Enchantment, Integer> entry : selectedEnchantments.entrySet()) {
            Enchantment ench = entry.getKey();
            int newLevel = entry.getValue();
            int originalLevel = originalEnchantments.getOrDefault(ench, 0);
            
            if (newLevel > originalLevel) {
                int levelsAdded = newLevel - originalLevel;
                cost += levelsAdded * getRarityCost(ench.getRarity());
            }
        }
        
        this.enchantingCost = cost;
        this.repairCost = calculateRepairCost();
    }
    
    private int getRarityCost(Enchantment.Rarity rarity) {
        switch (rarity) {
            case COMMON: return 1;
            case UNCOMMON: return 2;
            case RARE: return 4;
            case VERY_RARE: return 8;
            default: return 1;
        }
    }
    
    public boolean isEnchantmentCompatible(Enchantment ench, int level) {
        for (Map.Entry<Enchantment, Integer> entry : selectedEnchantments.entrySet()) {
            if (entry.getValue() > 0 && entry.getKey() != ench) {
                if (!entry.getKey().isCompatibleWith(ench)) return false;
            }
        }
        return true;
    }
    
    public boolean canEnchant(EntityPlayer player) { 
        return !getStackInSlot(0).isEmpty() && enchantmentsChanged && (player.experienceLevel >= enchantingCost || player.capabilities.isCreativeMode);
    }
    
    public boolean canRepair(EntityPlayer player) { 
        ItemStack stack = getStackInSlot(0);
        return !stack.isEmpty() && stack.isItemDamaged() && (player.experienceLevel >= repairCost || player.capabilities.isCreativeMode);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
        this.tickCount = compound.getInteger("TickCount");
        this.bookRotation = compound.getFloat("BookRotation");
        this.oBookRotation = compound.getFloat("OBookRotation");
        this.pageFlip = compound.getFloat("PageFlip");
        this.oPageFlip = compound.getFloat("OPageFlip");
        this.bookSpread = compound.getFloat("BookSpread");
        this.oBookSpread = compound.getFloat("OBookSpread");
        this.flipRandom = compound.getFloat("FlipRandom");
        this.flipRotation = compound.getFloat("FlipRotation");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Inventory", inventory.serializeNBT());
        compound.setInteger("TickCount", this.tickCount);
        compound.setFloat("BookRotation", this.bookRotation);
        compound.setFloat("OBookRotation", this.oBookRotation);
        compound.setFloat("PageFlip", this.pageFlip);
        compound.setFloat("OPageFlip", this.oPageFlip);
        compound.setFloat("BookSpread", this.bookSpread);
        compound.setFloat("OBookSpread", this.oBookSpread);
        compound.setFloat("FlipRandom", this.flipRandom);
        compound.setFloat("FlipRotation", this.flipRotation);
        return compound;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, net.minecraft.util.EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        }
        return super.getCapability(capability, facing);
    }
}