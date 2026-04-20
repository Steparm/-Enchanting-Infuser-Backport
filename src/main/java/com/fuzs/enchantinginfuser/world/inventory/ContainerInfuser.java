package com.fuzs.enchantinginfuser.world.inventory;

import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

public class ContainerInfuser extends Container {

    public final TileEntityInfuser tileEntity;
    private final boolean isAdvanced;

    public ContainerInfuser(InventoryPlayer playerInv, World world, BlockPos pos, boolean isAdvanced) {
        this.isAdvanced = isAdvanced;
        this.tileEntity = (TileEntityInfuser) world.getTileEntity(pos);

        this.addSlotToContainer(new Slot(tileEntity, 0, 8, 31) {
            @Override
            public void onSlotChanged() {
                tileEntity.onItemUpdated();
                super.onSlotChanged();
            }
            
            @Override
            public int getSlotStackLimit() {
                return 1;
            }
            
            @Override
            public boolean isItemValid(ItemStack stack) {
                return true;
            }
            
            @Override
            public boolean canTakeStack(EntityPlayer player) {
                return !tileEntity.enchantmentsChanged;
            }
            
            @Override
            public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
                tileEntity.enchantmentsChanged = false;
                tileEntity.originalStack = ItemStack.EMPTY;
                tileEntity.selectedEnchantments.clear();
                return super.onTake(thePlayer, stack);
            }
        });

        this.addSlotToContainer(new SlotArmor(playerInv, 39, 8, 103, EntityEquipmentSlot.HEAD));
        this.addSlotToContainer(new SlotArmor(playerInv, 38, 8, 121, EntityEquipmentSlot.CHEST));
        this.addSlotToContainer(new SlotArmor(playerInv, 37, 196, 103, EntityEquipmentSlot.LEGS));
        this.addSlotToContainer(new SlotArmor(playerInv, 36, 196, 121, EntityEquipmentSlot.FEET));

        this.addSlotToContainer(new Slot(playerInv, 40, 8, 161) {
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 30 + j * 18, 103 + i * 18));
            }
        }
        
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInv, k, 30 + k * 18, 161));
        }
    }

    private static class SlotArmor extends Slot {
        private final EntityEquipmentSlot armorType;
        public SlotArmor(InventoryPlayer inventory, int index, int x, int y, EntityEquipmentSlot armorType) {
            super(inventory, index, x, y);
            this.armorType = armorType;
        }
        @Override
        public int getSlotStackLimit() { return 1; }
        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem().isValidArmor(stack, armorType, ((InventoryPlayer)this.inventory).player);
        }
        @SideOnly(Side.CLIENT)
        public String getSlotTexture() { return ItemArmor.EMPTY_SLOT_NAMES[armorType.getIndex()]; }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileEntity.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot clickSlot = this.inventorySlots.get(idx);

        if (clickSlot != null && clickSlot.getHasStack()) {
            itemStack = clickSlot.getStack();
            ItemStack originalStack = itemStack.copy();

            if (idx == 0) {
                if (!this.mergeItemStack(itemStack, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
                clickSlot.onSlotChange(itemStack, originalStack);
            } else if (idx >= 1 && idx < 6) {
                if (!this.mergeItemStack(itemStack, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (idx >= 6 && idx < 42) {
                if (this.inventorySlots.get(0).isItemValid(itemStack)) {
                    if (!this.mergeItemStack(itemStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (idx >= 6 && idx < 33) {
                    if (!this.mergeItemStack(itemStack, 33, 42, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (idx >= 33 && idx < 42) {
                    if (!this.mergeItemStack(itemStack, 6, 33, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemStack.isEmpty()) {
                clickSlot.putStack(ItemStack.EMPTY);
            } else {
                clickSlot.onSlotChanged();
            }

            if (itemStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            clickSlot.onTake(player, itemStack);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            listener.sendWindowProperty(this, 0, tileEntity.enchantingCost);
            listener.sendWindowProperty(this, 1, tileEntity.enchantingPower);
            listener.sendWindowProperty(this, 2, tileEntity.repairCost);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (id == 0) tileEntity.enchantingCost = data;
        else if (id == 1) tileEntity.enchantingPower = data;
        else if (id == 2) tileEntity.repairCost = data;
    }

    public List<Map.Entry<Enchantment, Integer>> getSortedEntries() {
        List<Map.Entry<Enchantment, Integer>> list = new java.util.ArrayList<>(tileEntity.selectedEnchantments.entrySet());
        list.sort(java.util.Comparator.comparing(e -> e.getKey().getName()));
        return list;
    }
}