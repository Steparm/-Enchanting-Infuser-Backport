package com.fuzs.enchantinginfuser.tileentity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.*;

public class TileEntityInfuser extends TileEntity implements ITickable, IInventory {
    
    private NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    
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
    
    @Override
    public void update() {
        this.oBookRotation = this.bookRotation;
        this.oPageFlip = this.pageFlip;
        this.oBookSpread = this.bookSpread;
        this.tickCount++;
        
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
    
    @Override
    public int getSizeInventory() {
        return 1;
    }
    
    @Override
    public boolean isEmpty() {
        return inventory.get(0).isEmpty();
    }
    
    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }
    
    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }
    
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index);
    }
    
    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        if (stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }
    
    @Override
    public int getInventoryStackLimit() {
        return 1;
    }
    
    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    
    @Override
    public void openInventory(EntityPlayer player) {}
    
    @Override
    public void closeInventory(EntityPlayer player) {}
    
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }
    
    @Override
    public int getField(int id) {
        return 0;
    }
    
    @Override
    public void setField(int id, int value) {}
    
    @Override
    public int getFieldCount() {
        return 0;
    }
    
    @Override
    public void clear() {
        inventory.clear();
    }
    
    @Override
    public String getName() {
        return "container.enchanting_infuser";
    }
    
    @Override
    public boolean hasCustomName() {
        return false;
    }
    
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
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
        if (stack.isEmpty() || stack.getItemDamage() == 0) return 0;
        return Math.max(1, stack.getItemDamage() / 100);
    }
    
    public void updateEnchantment(Enchantment ench, int newLevel) {
        if (getStackInSlot(0).isEmpty()) return;
        
        int maxAllowedLevel = getMaxLevelForEnchantment(ench);
        if (newLevel >= 0 && newLevel <= ench.getMaxLevel() && newLevel <= maxAllowedLevel) {
            selectedEnchantments.put(ench, newLevel);
            this.enchantmentsChanged = true;
            calculateCost();
        }
    }
    
    public void enchantItem(EntityPlayer player) {
        if (!canEnchant(player)) return;
        
        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-enchantingCost);
        }
        
        Map<Enchantment, Integer> toApply = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : selectedEnchantments.entrySet()) {
            if (entry.getValue() > 0) {
                toApply.put(entry.getKey(), entry.getValue());
            }
        }
        
        ItemStack newStack = originalStack.copy();
        if (newStack.getItem() == Items.BOOK && !toApply.isEmpty()) {
            newStack = new ItemStack(Items.ENCHANTED_BOOK);
        }
        EnchantmentHelper.setEnchantments(toApply, newStack);
        
        setInventorySlotContents(0, newStack);
        
        originalStack = newStack.copy();
        this.originalEnchantments = new HashMap<>(selectedEnchantments);
        this.enchantmentsChanged = false;
        calculateCost();
    }
    
    public void repairItem(EntityPlayer player) {
        ItemStack stack = getStackInSlot(0);
        if (stack.isEmpty() || stack.getItemDamage() == 0) return;
        
        if (!player.capabilities.isCreativeMode) {
            player.addExperienceLevel(-repairCost);
        }
        
        ItemStack newStack = stack.copy();
        newStack.setItemDamage(0);
        setInventorySlotContents(0, newStack);
        
        originalStack = newStack.copy();
        enchantmentsChanged = false;
        loadEnchantmentsFromItem();
    }
    
    private void updatePower() {
        this.enchantingPower = getEnchantingPower();
    }
    
    public int getEnchantingPower() {
        int power = 0;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -2; k <= 2; ++k) {
                    if (i == 0 && k == 0) continue;
                    BlockPos checkPos = pos.add(i, j, k);
                    if (world != null && world.getBlockState(checkPos).getBlock() instanceof net.minecraft.block.BlockBookshelf) {
                        power++;
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
        
        this.enchantingCost = cost == 0 ? 1 : cost;
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
        return !stack.isEmpty() && stack.getItemDamage() > 0 && (player.experienceLevel >= repairCost || player.capabilities.isCreativeMode);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, inventory);
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
        ItemStackHelper.saveAllItems(compound, inventory);
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
}